package org.zy.fluorite.beans.factory.support;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.exception.BeanCreationException;
import org.zy.fluorite.beans.factory.exception.BeanDefinitionStoreException;
import org.zy.fluorite.beans.factory.exception.ImplicitlyAppearedSingletonException;
import org.zy.fluorite.beans.factory.exception.UnsatisfiedDependencyException;
import org.zy.fluorite.beans.factory.interfaces.BeanWrapper;
import org.zy.fluorite.beans.support.ConstructorArgumentValues;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.exception.FluoriteRuntimeException;
import org.zy.fluorite.core.interfaces.ParameterNameDiscoverer;
import org.zy.fluorite.core.subject.ExecutableParameter;
import org.zy.fluorite.core.subject.NamedThreadLocal;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.AutowireUtils;
import org.zy.fluorite.core.utils.ClassUtils;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月11日 下午10:59:15;
 * @Description 构造器解析器，定义了构造器解析的具体逻辑
 */
public class ConstructorResolver {
	private final Logger logger;

	private static final Object[] EMPTY_ARGS = new Object[0];

	private final AbstractAutowireCapableBeanFactory beanFactory;

	private static final NamedThreadLocal<InjectionPoint> currentInjectionPoint = new NamedThreadLocal<>(
			"Current injection point");

	public static InjectionPoint getCurrentInjectionPoint() {
		return currentInjectionPoint.get();
	}

	public static InjectionPoint setCurrentInjectionPoint(InjectionPoint injectionPoint) {
		InjectionPoint old = currentInjectionPoint.get();
		if (injectionPoint != null) {
			currentInjectionPoint.set(injectionPoint);
		} else {
			currentInjectionPoint.remove();
		}
		return old;
	}

	public ConstructorResolver(AbstractAutowireCapableBeanFactory beanFactory) {
		super();
		this.beanFactory = beanFactory;
		this.logger = beanFactory.getLogger();
	}

	public BeanWrapper instantiateUsingFactoryMethod(String beanName, RootBeanDefinition mbd, Object[] args) {
		BeanWrapperImpl bw = new BeanWrapperImpl();

		Object factoryBean;
		Class<?> factoryClass;
		boolean isStatic;

		// 获得要使用的FactoryBean实现的bean名称
		String factoryBeanName = mbd.getFactoryBeanName();

		if (factoryBeanName != null) {
			if (factoryBeanName.equals(beanName)) {
				throw new BeanDefinitionStoreException("工厂bean引用指向同一个bean定义，by：" + beanName);
			}
			DebugUtils.log(logger, "当前Bean通过工厂方法进行实例化，by name：" + beanName + "，factoryBeanName：" + factoryBeanName);
			factoryBean = this.beanFactory.getBean(factoryBeanName);
			// Bean模型为单例且当前BeanFactory已存在此单例则抛出异常
			if (mbd.isSingleton() && this.beanFactory.containsSingleton(beanName)) {
				throw new ImplicitlyAppearedSingletonException("隐式的出现单例异常，通常发生在通过多种方式注册同一个Bean的情况下。by：" + beanName);
			}
			factoryClass = factoryBean.getClass();
			isStatic = false;
		} else {
			factoryBean = null;
			factoryClass = mbd.getBeanClass();
			isStatic = true;
		}
		// 要使用的工厂方法
		Method factoryMethodToUse = null;
		// 存储要使用的参数集
		ArgumentsHolder argsHolderToUse = null;
		// 要使用的参数集
		Object[] argsToUse = null;

		if (args != null) {
			argsToUse = args;
		} else {
			Object[] argsToResolve = null;
			synchronized (mbd.getConstructorArgumentLock()) {
				factoryMethodToUse = (Method) mbd.getResolvedConstructorOrFactoryMethod();
				// 当前bean使用构造器或工厂方法进行实例化且已解析构造器参数
				if (factoryMethodToUse != null && mbd.isConstructorArgumentsResolved()) {
					// 已解析的构造器参数集
					argsToUse = mbd.getResolvedConstructorArguments().toArray();
					if (argsToUse == null) {
						argsToResolve = mbd.getPreparedConstructorArguments().toArray();
					}
				}
			}
			if (argsToResolve != null) {
				/**
				 * 若有准备好的构造器参数集，那么就进行解析 (此处不使用XML配置Bean是不大可能会执行的)
				 */
				logger.info("流程处理中调用了为实现的方法resolvePreparedArguments()，by：" + beanName);
				throw new FluoriteRuntimeException("未准备好解析构造器参数集，特抛出此异常");
				// argsToUse = (beanName, mbd, bw, factoryMethodToUse, argsToResolve, true);
			}
		}

		boolean autowiring = (mbd.getResolvedAutowireMode() == AutowireUtils.AUTOWIRE_CONSTRUCTOR);
		ConstructorArgumentValues resolvedValues = null;

		if (factoryMethodToUse == null) { // 在创建BeanDefinition过程中未保存工厂方法对象
			// 若factoryClass是cglib生成的增强型子类Class对象，那么就返回其父类
			factoryClass = ClassUtils.getUserClass(factoryClass);
			// 若RootBeanDefinition允许非公共访问，那么将返回子类和父类的所有方法对象，反之则只返回此类的公共方法
			Method[] rawCandidates = mbd.isNonPublicAccessAllowed() ? factoryClass.getDeclaredMethods()
					: factoryClass.getMethods();
			List<Method> candidateSet = new ArrayList<>();
			for (Method candidate : rawCandidates) {
				/**
				 * 1.检查当前方法对象是否是静态的 2.检查当前方法名与RootBeanDefinition之中的factoryMethodName属性的一致性
				 * 满足以上条件则将此方法对象添加到候选集中
				 */
				if (Modifier.isStatic(candidate.getModifiers()) == isStatic && mbd.isFactoryMethod(candidate)) {
					candidateSet.add(candidate);
				}
			}
			// 对给定的工厂方法进行排序，优先使用公共方法，而“贪婪”的方法使用最多的参数。结果将首先包含公共方法，参数数量减少，然后是非公共方法，参数数量减少
			AutowireUtils.sortFactoryMethods(candidateSet);

			int minNrOfArgs;
			if (args != null) {
				minNrOfArgs = args.length;
			} else {
				// 我们没有以编程方式传入参数，因此需要解析bean定义中的构造函数参数中指定的参数.
				if (mbd.hasConstructorArgumentValues()) {
					ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
					resolvedValues = new ConstructorArgumentValues();
					// 将此bean的构造函数参数解析为resolvedValues对象。这可能涉及查找其他bean。此方法还用于处理静态工厂方法的调用
					minNrOfArgs = cargs.getArgumentCount();
				} else {
					minNrOfArgs = 0;
				}
			}

			for (Method candidate : candidateSet) {
				Class<?>[] paramTypes = candidate.getParameterTypes();
				if (paramTypes.length >= minNrOfArgs) {
					// 第一个进入此逻辑的为最优先的工厂方法
					ArgumentsHolder argsHolder;
					if (args != null) {
						// 给定的显式参数->参数长度必须完全匹配
						if (paramTypes.length != args.length) {
							continue;
						}
						argsHolder = new ArgumentsHolder(args);
					} else {
						try {
							String[] paramNames = null;
							ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
							if (pnd != null) {
								paramNames = pnd.getParameterNames(candidate);
							}
							// 根据已解析的构造函数参数值，创建参数数组以调用构造函数或工厂方法
							argsHolder = createArgumentArray(beanName, mbd, resolvedValues, bw, paramTypes, paramNames,
									candidate, autowiring);
						} catch (UnsatisfiedDependencyException ex) {
							if (DebugUtils.debug) {
								logger.info("忽略工厂方法 ‘" + candidate + "’ by bean：" + beanName);
							}
							continue;
						}
					}

					if (argsHolder != null && argsHolder.arguments.length > 0) {
						argsHolderToUse = argsHolder;
						// 找到合适的参数值之后结束当前循环
						break;
					}
				}
			}

			if (args == null && argsHolderToUse != null) {
				// 缓存构造器参数
				argsHolderToUse.storeCache(mbd, factoryMethodToUse);
			}
		} else if (factoryMethodToUse != null && factoryMethodToUse.getParameterCount() > 0 && argsToUse == null) { // 在创建BeanDefinition对象时已保存工厂方法对象但未解析方法参数
			resolvedValues = new ConstructorArgumentValues();
			String[] paramNames = null;
			ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
			if (pnd != null) {
				// 获得参数名
				paramNames = pnd.getParameterNames(factoryMethodToUse);
			}
			// 根据已解析的构造函数参数值，创建参数数组以调用构造函数或工厂方法
			argsHolderToUse = createArgumentArray(beanName, mbd, resolvedValues, bw,
					factoryMethodToUse.getParameterTypes(), paramNames, factoryMethodToUse, autowiring);
			if (args == null && argsHolderToUse != null) {
				// 缓存构造器参数
				argsHolderToUse.storeCache(mbd, factoryMethodToUse);
				argsToUse = argsHolderToUse.arguments;
			}
		}
		try {
			Object beanInstance = this.beanFactory.getInstantiationStrategy().instantiate(mbd, beanName,
					this.beanFactory, factoryBean, factoryMethodToUse, argsToUse);
			// 将实例化的bean保存到包装类中
			bw.setBeanInstance(beanInstance);
		} catch (Throwable ex) {
			throw new BeanCreationException("通过工厂方法实例化Bean失败，by beanName：" + beanName + "，factoryClass：" + factoryClass
					+ "，factoryMethodToUse：" + factoryMethodToUse + "，argsToUse："
					+ (argsToUse == null ? null : Arrays.asList(argsToUse)), ex);
		}
		return bw;
	}

	private Object instantiate(String beanName, RootBeanDefinition mbd, Constructor<?> cots, Object[] args) {
		return this.beanFactory.getInstantiationStrategy().instantiate(mbd, beanName,
				this.beanFactory.parentBeanFactory, cots, args);
	}

	public BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mbd, Constructor<?>[] ctors,
			Object[] args) {

		BeanWrapper bw = new BeanWrapperImpl();

		Constructor<?> constructorToUse = null;
		ArgumentsHolder argsHolderToUse = null;
		Object[] argsToUse = null;

		if (constructorToUse == null || argsToUse == null) {
			// 使用指定的构造器
			Constructor<?>[] candidates = ctors;
			if (candidates == null) {
				Class<?> beanClass = mbd.getBeanClass();
				try {
					// 此处为true则返回beanClass所有的构造器
					candidates = (mbd.isNonPublicAccessAllowed() ? beanClass.getDeclaredConstructors()
							: beanClass.getConstructors());
				} catch (Throwable ex) {
					throw new BeanCreationException("获取构造器函数时抛出异常，by Class：" + beanClass.getName(), ex);
				}
			}

			// 只有一个无参候选构造器
			if (candidates.length == 1 && args == null && !mbd.hasConstructorArgumentValues()) {
				Constructor<?> uniqueCandidate = candidates[0];
				if (uniqueCandidate.getParameterCount() == 0) {
					synchronized (mbd.getConstructorArgumentLock()) {
						mbd.setResolvedConstructorOrFactoryMethod(uniqueCandidate);
						mbd.setConstructorArgumentsResolved(true);
						mbd.setResolvedConstructorArguments(EMPTY_ARGS);
					}
					bw.setBeanInstance(instantiate(beanName, mbd, uniqueCandidate, EMPTY_ARGS));
					return bw;
				}
			}

			// 是否需要解析构造器.
			boolean autowiring = (ctors != null || mbd.getResolvedAutowireMode() == AutowireUtils.AUTOWIRE_CONSTRUCTOR);
			ConstructorArgumentValues cargs = null;
			// 构造器所需参数的最小个数
			int minNrOfArgs;
			if (args != null) {
				minNrOfArgs = args.length;
			} else {
				cargs = mbd.getConstructorArgumentValues();
				// 确定最小参数个数
				minNrOfArgs = cargs.getArgumentCount();
			}

			// 候选构造器排序，公共构造器优先于非公共构造器，参数个数小的优先于参数个数大的
			AutowireUtils.sortConstructors(candidates);

			for (Constructor<?> candidate : candidates) {
				int parameterCount = candidate.getParameterCount();

				if (parameterCount < minNrOfArgs) {
					continue;
				}
				// 因为之前排序的缘故，所以第一个执行到此的候选构造器为最优先的构造器
				ArgumentsHolder argsHolder;
				// 获得当前候选构造器的参数类型集合
				Class<?>[] paramTypes = candidate.getParameterTypes();
				if (!cargs.isEmpty()) {
					// 检查当前构造器上是否标注@ConstructorProperties注解，标注则返回value值
					String[] paramNames = ConstructorPropertiesChecker.evaluate(candidate, parameterCount);
					if (paramNames == null) {
						ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
						if (pnd != null) {
							// 获得参数名集合
							paramNames = pnd.getParameterNames(candidate);
						}
					}
					argsHolder = createArgumentArray(beanName, mbd, cargs, bw, paramTypes, paramNames,
							getUserDeclaredConstructor(candidate), autowiring);
				} else {
					// 给定的显式参数->参数长度必须完全匹配
					if (parameterCount != args.length) {
						continue;
					}
					argsHolder = new ArgumentsHolder(args);
				}

				if (argsHolder != null && argsHolder.arguments.length > 0) {
					argsHolderToUse = argsHolder;
					constructorToUse = candidate;
					// 找到合适的参数值之后结束当前循环
					break;
				}
			}

			if (args == null && argsHolderToUse != null) {
				argsHolderToUse.storeCache(mbd, constructorToUse);
			}
		}
		
		argsToUse = argsHolderToUse.resolveNecessary ? argsHolderToUse.arguments : argsHolderToUse.rawArguments ;
		bw.setBeanInstance(instantiate(beanName, mbd, constructorToUse, argsToUse));
		return bw;
	}

	/**
	 * 根据已解析的构造函数参数值，创建参数数组以调用构造函数或工厂方法
	 * 
	 * @param beanName
	 * @param mbd
	 * @param resolvedValues
	 * @param bw
	 * @param paramTypes
	 * @param paramNames
	 * @param executable
	 * @param autowiring
	 * @param b
	 * @return
	 */
	private ArgumentsHolder createArgumentArray(String beanName, RootBeanDefinition mbd,
			ConstructorArgumentValues resolvedValues, BeanWrapper bw, Class<?>[] paramTypes, String[] paramNames,
			Executable executable, boolean autowiring) {
		ArgumentsHolder args = new ArgumentsHolder(paramTypes.length);
		Set<ConstructorArgumentValues.ValueHolder> usedValueHolders = new HashSet<>(paramTypes.length);
		// 自动注入的依赖项beanName
		Set<String> autowiredBeanNames = new LinkedHashSet<>(4);

		for (int paramIndex = 0; paramIndex < paramTypes.length; paramIndex++) {
			// paramType：interface javax.validation.Validator
			Class<?> paramType = paramTypes[paramIndex];
			// paramName：validator
			String paramName = (paramNames != null ? paramNames[paramIndex] : "");
			// 尝试查找匹配的构造函数参数值
			ConstructorArgumentValues.ValueHolder valueHolder = null;
			if (resolvedValues != null) {
				valueHolder = resolvedValues.getArgumentValue(paramIndex, paramType, paramName, usedValueHolders);
			}
			if (valueHolder != null) { // 不需要解析参数
				usedValueHolders.add(valueHolder);
				Object originalValue = valueHolder.getValue();
				Object convertedValue = null;
				if (valueHolder.isConverted()) { // 参数值已经过类型转换
					convertedValue = valueHolder.getConvertedValue();
					args.preparedArguments[paramIndex] = convertedValue;
				} else {
					// 类型转换判断，未通过则直接抛出异常
					if (paramType.isInstance(originalValue)) {
						convertedValue = originalValue;
						Object sourceHolder = valueHolder.getSource();
						if (sourceHolder instanceof ConstructorArgumentValues.ValueHolder) {
							Object sourceValue = ((ConstructorArgumentValues.ValueHolder) sourceHolder).getValue();
							args.resolveNecessary = true;
							args.preparedArguments[paramIndex] = sourceValue;
						}
					} else {
						throw new ClassCastException(
								"尝试将 " + originalValue + " 对象转换为 ‘" + paramType + "'类型失败。by ：" + executable);
					}
				}
				// 在此统一存储原始参数和类型转化之后的参数
				args.arguments[paramIndex] = convertedValue;
				args.rawArguments[paramIndex] = originalValue;
			} else {
				ExecutableParameter executableParameter = new ExecutableParameter(executable);
				try {
					// 获得参数值结果，可能是懒加载式的参数值
					Object autowiredArgument = resolveAutowiredArgument(executableParameter, beanName,
							autowiredBeanNames);
					args.rawArguments[paramIndex] = autowiredArgument;
					// 类型检查
					Assert.isAssignable(paramType, autowiredArgument.getClass());
					args.arguments[paramIndex] = autowiredArgument;
					args.preparedArguments[paramIndex] = new Object();
					// resolveNecessary：解决必要问题
					args.resolveNecessary = true;
				} catch (BeansException ex) {
					throw new UnsatisfiedDependencyException("未满足条件的依赖项异常，by：" + executable, ex);
				}

			}
		}
		// 依赖注册，在属性填充时使用
		for (String autowiredBeanName : autowiredBeanNames) {
			this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
			if (DebugUtils.debug) {
				logger.info("按类型自动注入" + (executable instanceof Constructor ? "构造器[" : "工厂方法[") + executable.getName()
						+ "]，by autowiredBeanName：" + autowiredBeanName);
			}
		}

		return args;
	}

	protected Object resolveAutowiredArgument(ExecutableParameter param, String beanName,
			Set<String> autowiredBeanNames) {
		if (InjectionPoint.class.isAssignableFrom(param.getParameterType(autowiredBeanNames.size()))) {
			InjectionPoint injectionPoint = currentInjectionPoint.get();
			if (injectionPoint == null) {
				throw new IllegalStateException("当前没有可用的注入点： " + param);
			}
			return injectionPoint;
		}
		/**
		 * DependencyDescriptor为InjectionPoint的子类，即将注入的特定依赖项的描述符。
		 * 包装构造函数参数、方法参数或字段，允许对其元数据进行统一访问。
		 */
		return this.beanFactory.resolveDependency(new DependencyDescriptor(param,autowiredBeanNames.size(), true), beanName, autowiredBeanNames);
	}

	/**
	 * 若指定类对象是Cglib生成的子类则获取其父类的Class对象重新获取指定参数类型的构造器
	 * 
	 * @param constructor
	 * @return
	 */
	protected Constructor<?> getUserDeclaredConstructor(Constructor<?> constructor) {
		Class<?> declaringClass = constructor.getDeclaringClass();
		Class<?> userClass = ClassUtils.getUserClass(declaringClass);
		if (userClass != declaringClass) {
			try {
				return userClass.getDeclaredConstructor(constructor.getParameterTypes());
			} catch (NoSuchMethodException ex) {
			}
		}
		return constructor;
	}

	/**
	 * @ConstructorProperties注解检查
	 */
	private static class ConstructorPropertiesChecker {

		/**
		 * 解析此构造器是否被@ConstructorProperties注解所标注，是则返回其属性值且判断与实际需要的参数个数是否匹配，不匹配则抛出异常
		 * ，为标注则返回null
		 * 
		 * @param candidate
		 * @param paramCount
		 * @return
		 */
		public static String[] evaluate(Constructor<?> candidate, int paramCount) {
			ConstructorProperties cp = candidate.getAnnotation(ConstructorProperties.class);
			if (cp != null) {
				String[] names = cp.value();
				if (names.length != paramCount) {
					throw new IllegalStateException("使用@ConstructorProperties注释的构造函数 '" + candidate
							+ "'其参数个数与实际参数数不对应，by 实际参数个数： (" + paramCount + ") ");
				}
				return names;
			} else {
				return null;
			}
		}
	}

	/**
	 * 封装方法参数
	 */
	private static class ArgumentsHolder {
		/** 原始参数 */
		public final Object[] rawArguments;
		/** 类型转换之后的原始参数 */
		public final Object[] arguments;
		/** 准备好的参数，存储ValueHolder对象的值 */
		public final Object[] preparedArguments;

		public boolean resolveNecessary = false;

		public ArgumentsHolder(int size) {
			this.rawArguments = new Object[size];
			this.arguments = new Object[size];
			this.preparedArguments = new Object[size];
		}

		public ArgumentsHolder(Object[] args) {
			this.rawArguments = args;
			this.arguments = args;
			this.preparedArguments = args;
		}

		/**
		 * 将解析的方法或构造器相关属性保存到RootBeanDefinition对象中
		 * 
		 * @param mbd
		 * @param constructorOrFactoryMethod
		 */
		public void storeCache(RootBeanDefinition mbd, Executable constructorOrFactoryMethod) {
			synchronized (mbd.getConstructorArgumentLock()) {
				mbd.setResolvedConstructorOrFactoryMethod(constructorOrFactoryMethod);
				mbd.setConstructorArgumentsResolved(true);
				if (this.resolveNecessary) {
					// 经过类型转换之后的参数
					mbd.setPreparedConstructorArguments(this.arguments);
				} else {
					// 保存原始参数
					mbd.setResolvedConstructorArguments(this.rawArguments);
				}
			}
		}
	}

}

package org.zy.fluorite.context.support;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.aware.BeanFactoryAware;
import org.zy.fluorite.beans.factory.exception.BeanCreationException;
import org.zy.fluorite.beans.factory.exception.UnsatisfiedDependencyException;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.processor.MergedBeanDefinitionPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.SmartInstantiationAwareBeanPostProcessor;
import org.zy.fluorite.beans.factory.support.DependencyDescriptor;
import org.zy.fluorite.beans.factory.support.InjectionMetadata;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.beans.interfaces.PropertyValues;
import org.zy.fluorite.beans.support.LookupOverride;
import org.zy.fluorite.core.annotation.Autowired;
import org.zy.fluorite.core.annotation.Lookup;
import org.zy.fluorite.core.annotation.Value;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.subject.ExecutableParameter;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月14日 下午10:45:04;
 * @Description 自动注入注解解析后处理器
 */
public class AutowiredAnnotationBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor,
		MergedBeanDefinitionPostProcessor, BeanFactoryAware {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected ConfigurableListableBeanFactory beanFactory;

	protected ConfigurableEnvironment environment;

	/** 自动注入注解类型 */
	// private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(4);

	/** */
	private final Set<String> lookupMethodsChecked = Collections.newSetFromMap(new ConcurrentHashMap<>(256));

	/** 候选构造器集合 【构造器定义类Class对象 : 候选构造器集合】 */
	private final Map<Class<?>, Constructor<?>[]> candidateConstructorsCache = new ConcurrentHashMap<>(256);

	/** 注入元数据缓存 【注入目标属性名 : 注入属性】 */
	private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

	
	public AutowiredAnnotationBeanPostProcessor() {
		super();
	}

	public AutowiredAnnotationBeanPostProcessor(ConfigurableEnvironment environment) {
		// this.autowiredAnnotationTypes.add(Autowired.class);
		// this.autowiredAnnotationTypes.add(Value.class);
		setEnvironment(environment);
	}

	public void setAutowiredAnnotationType(Class<? extends Annotation> autowiredAnnotationType) {
		Assert.notNull(autowiredAnnotationType, "'autowiredAnnotationType'不能为null");
		// this.autowiredAnnotationTypes.clear();
		// this.autowiredAnnotationTypes.add(autowiredAnnotationType);
	}

	public Object resolvedCachedArgument(String beanName, Object cachedValue) {
		if (cachedValue instanceof DependencyDescriptor) {
			DependencyDescriptor descriptor = (DependencyDescriptor) cachedValue;
			Assert.isTrue(this.beanFactory != null, "没有可用的BeanFactory实现");
			return this.beanFactory.resolveDependency(descriptor, beanName, new HashSet<String>());
		} else {
			return cachedValue;
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"AutowiredAnnotationBeanPostProcessor的BeanFactory引用需一个ConfigurableListableBeanFactory实现 : "
							+ beanFactory);
		}
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	public void setEnvironment(ConfigurableEnvironment environment) {
		this.environment = environment;
	}
	
	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		InjectionMetadata metadata = findAutowiringMetadata(beanDefinition, beanType, null);
		metadata.checkConfigMembers(beanDefinition);
	}

	@Override
	public void resetBeanDefinition(String beanName) {
		this.lookupMethodsChecked.remove(beanName);
		this.injectionMetadataCache.remove(beanName);
	}

	@Override
	public Constructor<?>[] determineCandidateConstructors(final BeanDefinition beanDefinition) throws BeansException {
		AnnotationMetadata annotationMetadata = beanDefinition.getAnnotationMetadata();
		Class<?> beanClass = beanDefinition.getBeanClass();
		String beanName = beanDefinition.getBeanName();
		RootBeanDefinition mbd = (RootBeanDefinition) beanDefinition;

		if (!this.lookupMethodsChecked.contains(beanName)) {
			try {
				Class<?> targetClass = beanClass;
				do {
					ReflectionUtils.doWithLocalMethods(targetClass, method -> {
						if (method.getAnnotations().length == 0) {
							return;
						}

						Lookup lookup = annotationMetadata.getAnnotationForMethod(method, Lookup.class);
						if (lookup != null) {
							Assert.isTrue(this.beanFactory != null, "没有可用的BeanFactory实现");
							LookupOverride override = new LookupOverride(method, lookup.value());
							mbd.getMethodOverrides().addOverride(override);
						}
					});
					targetClass = targetClass.getSuperclass();
				} while (targetClass != null && targetClass != Object.class);

			} catch (IllegalStateException ex) {
				throw new BeanCreationException("查找方法解析失败，by ：" + beanName, ex);
			}
			this.lookupMethodsChecked.add(beanName);
		}

		Constructor<?>[] candidateConstructors = this.candidateConstructorsCache.get(beanClass);
		if (candidateConstructors == null) {
			synchronized (this.candidateConstructorsCache) {
				candidateConstructors = this.candidateConstructorsCache.get(beanClass);
				if (candidateConstructors == null) {
					Constructor<?>[] rawCandidates;
					try {
						// 获得当前Class对象定义的所有构造器
						rawCandidates = beanClass.getDeclaredConstructors();
					} catch (Throwable ex) {
						throw new BeanCreationException("获得类定义的构造函数失败，by class：" + beanClass.getName(), ex);
					}
					List<Constructor<?>> candidates = new ArrayList<>(rawCandidates.length);
					Constructor<?> requiredConstructor = null;
					Constructor<?> defaultConstructor = null;
					for (Constructor<?> candidate : rawCandidates) {
						// isSynthetic：如果此可执行文件是合成结构即由Java编译器引入，则返回true，反之则返回false
						if (candidate.isSynthetic()) {
							continue;
						}
						Autowired autowired = annotationMetadata.getAnnotationForConstructor(candidate,
								Autowired.class);

						if (autowired == null) {
							Class<?> userClass = ClassUtils.getUserClass(beanClass);
							if (userClass != beanClass) {
								try {
									// 获得原初类对象的构造器
									Constructor<?> superCtor = userClass
											.getDeclaredConstructor(candidate.getParameterTypes());
									autowired = annotationMetadata.getAnnotationForConstructor(superCtor,
											Autowired.class);
								} catch (NoSuchMethodException ex) {
									logger.info("获得超类定义的构造函数失败，by class：" + userClass.getName());
								}
							}
						}
						if (autowired != null) {
							if (requiredConstructor != null) {
								throw new BeanCreationException(
										"一个类中不允许有第二个自动注入构造器，即@Autowired只能标注其中一个构造器，by class： " + beanClass.getName());
							}
							boolean required = autowired.required();
							if (required) {
								if (!candidates.isEmpty()) {
									throw new BeanCreationException("已找到必须的自动注入的构造器，by：" + candidates);
								}
								requiredConstructor = candidate;
							}
							candidates.add(candidate);
						} else if (candidate.getParameterCount() == 0) {
							// 将无参构造器作为默认构造器
							defaultConstructor = candidate;
						}
					}
					// 已检查了所有构造器，准备包装候选构造器
					if (requiredConstructor == null) { // 将默认构造函数作为回退添加到可选构造函数列表中
						if (defaultConstructor != null) {
							candidates.add(defaultConstructor);
						} else if (candidates.size() == 1) {
							DebugUtils.log(logger, "当前类没有定义无参构造器，所以使用@Autowired标注的非必选构造器作为候选，by：" + candidates.get(0));
						}
					}
					DebugUtils.log(logger, "在'" + beanClass + "'中找到的候选构造器集：" + candidates);
					candidateConstructors = candidates.toArray(new Constructor<?>[0]);
					this.candidateConstructorsCache.put(beanClass, candidateConstructors);
				}
			}
		}
		return (candidateConstructors.length > 0 ? candidateConstructors : null);
	}

	@Override
	public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, BeanDefinition beanDefinition)
			throws BeansException {
		InjectionMetadata metadata = findAutowiringMetadata(beanDefinition, bean.getClass(), pvs);
		String beanName = beanDefinition.getBeanName();
		try {
			metadata.inject(bean, beanName, pvs);
		} catch (Throwable ex) {
			throw new BeanCreationException("自动注入依赖项失败，by：" + beanName, ex);
		}
		return pvs;
	}

	private InjectionMetadata findAutowiringMetadata(BeanDefinition beanDefinition, Class<?> beanType,
			PropertyValues pvs) {
		String beanName = beanDefinition.getBeanName();
		String cacheKey = (Assert.hasText(beanName) ? beanName : beanType.getName());
		InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
		if (InjectionMetadata.needsRefresh(metadata, beanType)) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(cacheKey);
				if (InjectionMetadata.needsRefresh(metadata, beanType)) {
					metadata = buildAutowiringMetadata(beanDefinition, beanType);
					this.injectionMetadataCache.put(cacheKey, metadata);
				}
			}
		}
		return metadata;
	}

	private InjectionMetadata buildAutowiringMetadata(BeanDefinition beanDefinition, Class<?> beanType) {
		List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
		AnnotationMetadata annotationMetadata = beanDefinition.getAnnotationMetadata();
		Class<?> targetClass = beanDefinition.getBeanClass();
		do {
			final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

			ReflectionUtils.doWithLocalFields(targetClass, field -> {
				if (field.getAnnotations().length == 0) 	return;
				
				AnnotationAttributes attributesForField = annotationMetadata.getAnnotationAttributesForField(field);

				Autowired autowired = attributesForField.getAnnotation(Autowired.class);
				if (autowired != null) {
					DebugUtils.log(logger, "在" + field.getDeclaringClass() + "'类中找到的自动注入属性：" 
							+ field.getName() + "，by："	+ autowired.annotationType().getSimpleName());
					if (Modifier.isStatic(field.getModifiers())) {
						logger.warn("@Autowired注解不能标注于静态属性，by field: " + field);
						return;
					}
					currElements.add(new AutowiredFieldElement(field, autowired.required()));
				} else {
					Value value = attributesForField.getAnnotation(Value.class);
					if (value != null) {
						DebugUtils.log(logger,"在'" + field.getDeclaringClass() + "'类中找到的自动注入属性：" + field.getName() + "，by 注解：" + value.annotationType().getSimpleName());
						if (Modifier.isStatic(field.getModifiers())) {
							logger.warn("@Value注解不能标注于静态属性，by field: " + field);
							return;
						}
						currElements.add(new ValueFieldElement(field, value.value()));
					}
				}
			});

			ReflectionUtils.doWithLocalMethods(targetClass, method -> {
				if (method.getAnnotations().length == 0)
					return;
				AnnotationAttributes attributesForMethod = annotationMetadata.getAnnotationAttributesForMethod(method);
				Autowired autowired = attributesForMethod.getAnnotation(Autowired.class);
				if (autowired != null) {
					DebugUtils.log(logger,
							"在" + method.getDeclaringClass() + "'类中找到的自动注入方法：" + method.getName() + "，by：" + autowired);
					if (Modifier.isStatic(method.getModifiers())) {
						logger.warn("Autowired注解不能标注于静态方法，by methods: " + method);
						return;
					}
					if (method.getParameterCount() == 0) {
						logger.warn("Autowired注解标注的方法需是拥有参数的，by method: " + method);
					}
					currElements.add(new AutowiredMethodElement(method, autowired.required()));
				} else {
					Value value = attributesForMethod.getAnnotation(Value.class);
					if (value != null) {
						DebugUtils.log(logger,
								"在" + method.getDeclaringClass() + "'类中找到的自动注入方法：" + method.getName() + "，by：" + value);
						if (Modifier.isStatic(method.getModifiers())) {
							logger.warn("@Value注解不能标注于静态方法，by methods: " + method);
							return;
						}
						if (method.getParameterCount() == 0) {
							logger.warn("@Value注解标注的方法需是拥有参数的，by method: " + method);
						}
						currElements.add(new AutowiredMethodElement(method, false));
					}
				}
			});

			elements.addAll(currElements);
			targetClass = targetClass.getSuperclass();
		} while (targetClass != null && targetClass != Object.class);

		return new InjectionMetadata(beanType, elements);
	}

	/**
	 * 将dependentBeanName指代的bean注册为beanName的依赖项
	 * 
	 * @param beanName
	 * @param autowiredBeanNames
	 */
	private void registerDependentBeans(String beanName, Set<String> autowiredBeanNames) {
		if (beanName != null) {
			for (String autowiredBeanName : autowiredBeanNames) {
				if (this.beanFactory != null && this.beanFactory.containsBean(autowiredBeanName)) {
					this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
				}
			}
		}
	}

	private class ValueFieldElement extends InjectionMetadata.InjectedElement {
		/** 存储注解属性值 */
		private final String val;
		/** 已解析的注入属性值 */
		private volatile Object cachedFieldValue;

		private volatile boolean cached = false;

		protected ValueFieldElement(Field field, String value) {
			super(field, null);
			this.val = value;
		}

		@Override
		protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
			Field field = (Field) this.member;
			Object value = null;
			
			if (this.cached && !(cachedFieldValue instanceof DependencyDescriptor)) {
				value = cachedFieldValue;
			}
			
			if (value == null) {
				if (val.indexOf("#") != -1) {
					value = environment.resolvePlaceholders(val);
					cachedFieldValue = value;
					this.cached = true;
				} else {
					LinkedHashSet<String> autowiredBeanNames = new LinkedHashSet<>(1);
					DependencyDescriptor desc = new DependencyDescriptor(field, true);
					Assert.isTrue(beanFactory != null, "没有可用的BeanFactory实现");
					if (this.cached) {
						value = resolvedCachedArgument(beanName, this.cachedFieldValue);
					} else {
						try {
							// 根据此工厂中定义的bean解析指定的依赖关系
							value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames);
						} catch (BeansException ex) {
							throw new UnsatisfiedDependencyException(
									"无法解析的依赖属性，by field：" + field.getName() + " class：" + field.getDeclaringClass(), ex);
						}
						synchronized (this) {
							if (!this.cached) {
								if (value != null) {
									registerDependentBeans(beanName, autowiredBeanNames);
									this.cachedFieldValue = new ShortcutDependencyDescriptor(desc, beanName);
									this.cached = true;
								}
							}
						}
					}
				}
			}
			
			if (value != null) {
				ReflectionUtils.makeAccessible(field);
				Class<?> type = field.getType();
				// 类型装换
				Object convert = beanFactory.getConversionServiceStrategy().convert(value, type);
				// 反射关联属性值
				field.set(bean, convert);
			}
		}
	}

	/**
	 * 封装注解标注的注入属性信息
	 */
	private class AutowiredFieldElement extends InjectionMetadata.InjectedElement {

		private final boolean required;

		private volatile boolean cached = false;

		private volatile Object cachedFieldValue;

		/**
		 * 包装@auto
		 * 
		 * @param field
		 * @param required
		 */
		public AutowiredFieldElement(Field field, boolean required) {
			super(field, null);
			this.required = required;
		}

		@Override
		protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
			Field field = (Field) this.member;
			Object value = null;
			LinkedHashSet<String> autowiredBeanNames = new LinkedHashSet<>(1);
			DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
			Assert.isTrue(beanFactory != null, "没有可用的BeanFactory实现");
			if (this.cached) {
				value = resolvedCachedArgument(beanName, this.cachedFieldValue);
			} else {
				try {
					// 根据此工厂中定义的bean解析指定的依赖关系
					value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames);
				} catch (BeansException ex) {
					throw new UnsatisfiedDependencyException(
							"无法解析的依赖属性，by field：" + field.getName() + " class：" + field.getDeclaringClass(), ex);
				}
				synchronized (this) {
					if (!this.cached) {
						if (value != null || this.required) {
							registerDependentBeans(beanName, autowiredBeanNames);
							this.cachedFieldValue = new ShortcutDependencyDescriptor(desc, beanName);
							this.cached = true;
						}
					}
				}
			}
			if (value != null) {
				field.setAccessible(true);
				// 反射关联属性值
				field.set(bean, value);
			}
		}
	}

	/**
	 * 封装注解标注的注入方法信息
	 */
	private class AutowiredMethodElement extends InjectionMetadata.InjectedElement {

		private final boolean required;

		private volatile boolean cached = false;
		/** 存储注入方法的参数集合 */
		private volatile Object[] cachedMethodArguments;

		public AutowiredMethodElement(Method method, boolean required, PropertyDescriptor pd) {
			super(method, pd);
			this.required = required;
		}

		public AutowiredMethodElement(Method method, boolean required) {
			super(method, null);
			this.required = required;
		}

		@Override
		protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
			Method method = (Method) this.member;
			Object[] arguments;
			if (this.cached) {
				arguments = resolveCachedArguments(beanName);
			} else {
				int argumentCount = method.getParameterCount();
				arguments = new Object[argumentCount];
				DependencyDescriptor[] descriptors = new DependencyDescriptor[argumentCount];
				Set<String> autowiredBeans = new LinkedHashSet<>(argumentCount);
				Assert.isTrue(beanFactory != null, "没有可用的BeanFactory实现");
				for (int i = 0; i < argumentCount; i++) {
					ExecutableParameter executableParameter = new ExecutableParameter(method, i);
					DependencyDescriptor desc = new DependencyDescriptor(executableParameter, this.required);
					descriptors[i] = desc;
					try {
						// 若解析过程中无法找到合适的依赖项且此方法标注的@Autowried的required属性为true则抛出解析BeanNotOfRequiredTypeException
						Object arg = beanFactory.resolveDependency(desc, beanName, autowiredBeans);
						if (arg == null && !this.required) {
							arguments = null;
							if (DebugUtils.debug) {
								logger.info("非必须的注入方法参数解析依赖项之后为null，不在解析之后的参数，by parame：" + method.getParameters()[i]
										+ " method：" + method.getName() + " class：" + method.getDeclaringClass());
							}
							break;
						}
						arguments[i] = arg;
					} catch (BeansException ex) {
						throw new UnsatisfiedDependencyException("无法解析的依赖属性，by parame：" + method.getParameters()[i]
								+ " method：" + method.getName() + " class：" + method.getDeclaringClass(), ex);
					}
				}
				synchronized (this) {
					if (!this.cached) {
						if (arguments != null) {
							registerDependentBeans(beanName, autowiredBeans);
							this.cachedMethodArguments = descriptors;
							this.cached = true;
						} else {
							this.cachedMethodArguments = null;
						}
					}
				}
			}
			if (arguments != null) {
				try {
					ReflectionUtils.makeAccessible(method);
					method.invoke(bean, arguments);
				} catch (InvocationTargetException ex) {
					throw ex.getTargetException();
				}
			}
		}

		private Object[] resolveCachedArguments(String beanName) {
			Object[] cachedMethodArguments = this.cachedMethodArguments;
			if (cachedMethodArguments == null) {
				return null;
			}
			Object[] arguments = new Object[cachedMethodArguments.length];
			for (int i = 0; i < arguments.length; i++) {
				arguments[i] = resolvedCachedArgument(beanName, cachedMethodArguments[i]);
			}
			return arguments;
		}

	}

	/**
	 * 缓存已解析的DependencyDescriptor对象
	 */
	@SuppressWarnings("serial")
	private static class ShortcutDependencyDescriptor extends DependencyDescriptor {

		private final String shortcut;

		private final Class<?> requiredType;

		public ShortcutDependencyDescriptor(DependencyDescriptor original, String beanName) {
			super(original);
			this.shortcut = beanName;
			this.requiredType = original.getDependencyType();
		}

		public ShortcutDependencyDescriptor(ExecutableParameter executableParameter, String beanName,
				boolean required) {
			super(executableParameter, required);
			this.shortcut = beanName;
			this.requiredType = executableParameter.getParameterType();
		}

		@Override
		public Object resolveShortcut(BeanFactory beanFactory) {
			return beanFactory.getBean(this.shortcut, this.requiredType);
		}
	}
}

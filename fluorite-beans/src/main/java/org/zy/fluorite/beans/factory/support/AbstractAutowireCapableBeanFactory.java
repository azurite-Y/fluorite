package org.zy.fluorite.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.aware.BeanClassLoaderAware;
import org.zy.fluorite.beans.factory.aware.BeanFactoryAware;
import org.zy.fluorite.beans.factory.aware.BeanNameAware;
import org.zy.fluorite.beans.factory.exception.BeanCreationException;
import org.zy.fluorite.beans.factory.exception.BeanDefinitionStoreException;
import org.zy.fluorite.beans.factory.exception.BeanDefinitionValidationException;
import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.beans.factory.interfaces.AutowireCapableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.BeanWrapper;
import org.zy.fluorite.beans.factory.interfaces.InstantiationStrategy;
import org.zy.fluorite.beans.factory.interfaces.Scope;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.InstantiationAwareBeanPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.MergedBeanDefinitionPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.SmartInstantiationAwareBeanPostProcessor;
import org.zy.fluorite.beans.interfaces.AutowireCandidateResolver;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.beans.interfaces.PropertyValues;
import org.zy.fluorite.beans.support.MutablePropertyValues;
import org.zy.fluorite.core.convert.PrioritizedParameterNameDiscoverer;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.Aware;
import org.zy.fluorite.core.interfaces.ParameterNameDiscoverer;
import org.zy.fluorite.core.interfaces.function.ActiveFunction;
import org.zy.fluorite.core.interfaces.instantiation.InitializingBean;
import org.zy.fluorite.core.subject.NamedThreadLocal;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.AutowireUtils;
import org.zy.fluorite.core.utils.ClassUtils;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.ScopeUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月9日 下午4:29:46;
 * @Description
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
		implements AutowireCapableBeanFactory {

	/** 要忽略的依赖项类型。默认为空集 */
	protected final Set<Class<?>> ignoredDependencyTypes = new HashSet<>();

	/** 忽略的依赖项接口。默认只忽略BeanFactory接口。 */
	protected final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<>();

	/** 未完成的FactoryBean实例的缓存：FactoryBean名称到BeanWrapper */
	protected final ConcurrentMap<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

	/** 每个工厂类的候选工厂方法的缓存 */
	protected final ConcurrentMap<Class<?>, Method[]> factoryMethodCandidateCache = new ConcurrentHashMap<>();

	protected ParameterNameDiscoverer pnd = new PrioritizedParameterNameDiscoverer();

	private InstantiationStrategy instantiationStrategy = new SimpleInstantiationStrategy();

	protected AutowireCandidateResolver autowireCandidateResolver;

	private final NamedThreadLocal<String> currentlyCreatedBean = new NamedThreadLocal<>("Currently created bean");

	public AbstractAutowireCapableBeanFactory() {
		super();
	}

	public AbstractAutowireCapableBeanFactory(BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}

	@Override
	protected Object createBean(String beanName, RootBeanDefinition mbd, Object[] args) throws BeanCreationException {
		try {
			// 准备方法重写
			mbd.prepareMethodOverrides();
		} catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException("方法重写验证失败", ex);
		}
		try {
			// 让BeanPostProcessors有机会返回代理而不是目标bean实例，主要是AOP代理对象
			Object bean = resolveBeforeInstantiation(beanName, mbd);
			if (bean != null) {
				return bean;
			}
		} catch (Throwable ex) {
			throw new BeanCreationException("调用bean实例化前的BeanPostProcessor失败，by ：" + beanName, ex);
		}

		try {
			Object beanInstance = doCreateBean(beanName, mbd, args);
			DebugUtils.log(logger, "创建bean实例完成，by bean：" + beanInstance);
			return beanInstance;
		} catch (BeanCreationException ex) {
			// 以前检测到的异常，已具有正确的bean创建上下文，或者要传送到DefaultSingletonBeanRegistry的非法singleton状态.
			throw ex;
		} catch (Throwable ex) {
			throw new BeanCreationException("创建bean期间出现意外异常，by bean：" + beanName, ex);
		}
	}

	private Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
		Object bean = null;
		if (!mbd.getBeforeInstantiationResolved()) {
			bean = applyBeanPostProcessorsBeforeInstantiation(beanName, mbd);
			if (bean != null) {
				applyBeanPostProcessorsAfterInstantiation(bean, mbd);
			}
		}
		mbd.setBeforeInstantiationResolved(bean != null);
		return bean;
	}

	@Override
	public Object applyBeanPostProcessorsBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
		if (super.hasInstantiationAwareBeanPostProcessors) {
			for (InstantiationAwareBeanPostProcessor ibp : super.instantiationList) {
				Object result = ibp.postProcessBeforeInstantiation(beanName , mbd);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	@Override
	public Object applyBeanPostProcessorsAfterInitialization(Object bean, BeanDefinition mbd) throws BeansException {
		Object result = null;
		if (super.hasInstantiationAwareBeanPostProcessors) {
			for (BeanPostProcessor bp : super.beanPostProcessors) {
				result = bp.postProcessAfterInitialization(bean, mbd);
				if (result == null) {
					return bean;
				}
				bean = result;
			}
		}
		return bean;
	}

	@Override
	public Object applyBeanPostProcessorsBeforeInitialization(Object bean, BeanDefinition definition)
			throws BeansException {
		Object result = null;
		for (BeanPostProcessor bp : super.beanPostProcessors) {
			result = bp.postProcessBeforeInitialization(bean, definition);
			if (result == null) {
				return bean;
			}
			bean = result;
		}
		return bean;
	}

	@Override
	public void applyBeanPostProcessorsProperties(PropertyValues pvs, Object bean, BeanDefinition beanDefinition)
			throws BeansException {
		if (super.hasInstantiationAwareBeanPostProcessors) {
			for (InstantiationAwareBeanPostProcessor ibp : super.instantiationList) {
				pvs = ibp.postProcessProperties(pvs, bean, beanDefinition);
				if (pvs == null) {
					return;
				}
			}
		}
	}

	@Override
	public void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName) {
		if (super.hasMergedBeanDefinitionPostProcessors) { // 相等则表示已检查过所有BeanPostProcessor实现
			for (MergedBeanDefinitionPostProcessor ibp : super.mergedList) {
				ibp.postProcessMergedBeanDefinition(mbd, beanType, beanName);
			}
		}
	}

	@Override
	public boolean applyBeanPostProcessorsAfterInstantiation(Object bean, BeanDefinition bd) {
		if (super.hasInstantiationAwareBeanPostProcessors) {
			for (InstantiationAwareBeanPostProcessor ibp : super.instantiationList) {
				boolean flag = ibp.postProcessAfterInstantiation(bean, bd);
				if (!flag) {
					return flag;
				}
			}
		}
		return true;
	}

	protected Object getObjectForBeanInstance(Object beanInstance, String name, String beanName,
			RootBeanDefinition mbd) {
		// currentlyCreatedBean并未使用
		String currentlyCreatedBean = this.currentlyCreatedBean.get();
		if (currentlyCreatedBean != null) {
			/**
			 * 在Bean的初始化中，从IOC容器中获取不到依赖Bean才会显示的调用getBean方法实例化依赖Bean，
			 * 所以当前若有正在注册的Bean则表示当前Bean为此Bean的依赖项
			 */
			registerDependentBean(beanName, currentlyCreatedBean);
		}
		return super.getObjectForBeanInstance(beanInstance, name, beanName, mbd);
	}

	/**
	 * 具体的Bean实例创建和初始化操作
	 * 
	 * @param beanName
	 * @param mbd
	 * @param args
	 * @return
	 */
	protected Object doCreateBean(String beanName, RootBeanDefinition mbd, Object[] args) {
		BeanWrapper instanceWrapper = null;
		if (mbd.isSingleton()) {
			// factoryBeanInstanceCache - 未完成的FactoryBean实例的缓存：FactoryBean名称 : BeanWrapper
			instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
		}
		if (instanceWrapper == null) {
			instanceWrapper = createBeanInstance(beanName, mbd, args);
		}
		// 获得bean实例的包装对象
		final Object bean = instanceWrapper.getWrappedInstance();
		// 获得bean的Class
		Class<?> beanType = instanceWrapper.getWrappedClass();
		if (beanType != NullBean.class) {
			// 将实例化的bean的类型保存到RootBeanDefinition中
			mbd.setResolvedTargetType(beanType);
		}

		// 允许后处理器修改合并的bean定义.
		synchronized (mbd.getPostProcessingLock()) {
			// postProcessed - 是否已应用MergedBeanDefinitionPostProcessor实现类的bean后处理器标识
			if (!mbd.isPostProcessed()) {
				try {
					applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
				} catch (Throwable ex) {
					throw new BeanCreationException("调用合并BeanDefinition的后处理器发生异常，by：" + beanName, ex);
				}
				// 将当前bean是否进行了后处理的标识设为true
				mbd.setPostProcessed(true);
			}
		}

		// 急切地缓存singleton以便能够解析循环引用，即使是由生命周期接口（如BeanFactoryAware）触发时也是如此.
		boolean earlySingletonExposure = mbd.isSingleton() && super.isSingletonCurrentlyInCreation(beanName);
		if (earlySingletonExposure) {
			if (DebugUtils.debug) {
				logger.info("保存bean的前期引用，为引用解析做准备，by bean： " + beanName);
			}
			// 将当前beanName注册到单例对象BeanName集合中，且保存当前Bean的循环引用
			super.addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
		}

		// 初始化bean实例
		Object exposedObject = bean;
		try {
			// 使用RootBeanDefinition中的属性值填充给定BeanWrapper中的bean实例
			populateBean(beanName, mbd, instanceWrapper);
			exposedObject = initializeBean(beanName, exposedObject, mbd);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		// 将bean注册为一次性.
		try {
			registerDisposableBeanIfNecessary(beanName, bean, getBeanDefinition(beanName));
		} catch (BeanDefinitionValidationException ex) {
			throw new BeanCreationException("无效的销毁签名，by：" + beanName, ex);
		}
		return exposedObject;
	}

	/**
	 * 将给定的bean添加到此工厂中的可丢弃bean列表中， 注册其DisposableBean接口和或在工厂关闭时要调用的给定销毁方法。 只适用于单例。
	 * 
	 * @param beanName
	 * @param bean
	 * @param mbd
	 */
	protected void registerDisposableBeanIfNecessary(String beanName, Object bean, RootBeanDefinition mbd) {
		// 为生成RootBeanDefinition对象的Bean不纳入生命周期Bean
		if (mbd != null && !mbd.isPrototype() && super.hasDestructionAwareBeanPostProcessors
				&& bean.getClass() != NullBean.class && !mbd.getDestroyMethods().isEmpty()) {
			DebugUtils.log(logger, "将实例注册为可销毁Bean，by name：" + beanName);
			if (mbd.isSingleton()) {
				/**
				 * 注册一个DisposableBean实现，它执行给定bean的所有销毁工作：
				 * DestructionAwareBeanPostProcessors、DisposableBean接口、自定义销毁方法
				 */
				registerDisposableBean(beanName, new DisposableBeanAdapter(bean, beanName, mbd, this.destructionList));
			} else {
				Scope scope = this.scopes.get(mbd.getScope());
				if (scope == null) {
					throw new IllegalStateException("没有为作用域名称注册作用域" + mbd.getScope());
				}
				scope.registerDestructionCallback(beanName,
						new DisposableBeanAdapter(bean, beanName, mbd, super.destructionList));
			}
		}
	}

	protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args) {
		// FactoryMethodName - 如果要使用factory bean来创建bean,这里指定了相应的工厂bean的类名称
		if (mbd.getFactoryMethodName() != null) {
			return instantiateUsingFactoryMethod(beanName, mbd, args);
		}

		boolean resolved = false;
		boolean autowireNecessary = false;
		if (args == null) {
			synchronized (mbd.getConstructorArgumentLock()) {
				// resolvedConstructorOrFactoryMethod – 保存bean实例化的构造方法或bean工厂方法对象
				if (mbd.getResolvedConstructorOrFactoryMethod() != null) {
					resolved = true;
					// 包可见字段，标记为构造函数参数是否已解析
					autowireNecessary = mbd.isConstructorArgumentsResolved();
				}
			}
		}
		if (resolved) {
			if (autowireNecessary) {
				return autowireConstructor(beanName, mbd, null, null);
			} else {
				// 无特殊处理：只需使用无参构造器.
				return instantiateBean(beanName, mbd);
			}
		}

		// 通过Bean后处理器获得候选构造器数组
		Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(mbd);
		if (ctors != null || mbd.getResolvedAutowireMode() == AutowireUtils.AUTOWIRE_CONSTRUCTOR
				|| mbd.hasConstructorArgumentValues() || Assert.notNull(args)) {
			return autowireConstructor(beanName, mbd, ctors, args);
		}

		// 无特殊处理：只需使用无参构造器.
		return instantiateBean(beanName, mbd);
	}

	/**
	 * 通过Bean后处理器获得指定类的候选构造器数组
	 * 
	 * @param beanClass
	 * @param beanName
	 * @return
	 */
	protected Constructor<?>[] determineConstructorsFromBeanPostProcessors(BeanDefinition beanDefinition) {
		if (beanDefinition.getBeanClass() != null && super.hasSmartInstantiationAwareBeanPostProcessor) {
			for (SmartInstantiationAwareBeanPostProcessor sbp : super.smartInstantiationList) {
				Constructor<?>[] ctors = sbp.determineCandidateConstructors(beanDefinition);
				if (ctors != null) {
					return ctors;
				}
			}
		}
		return null;
	}

	/**
	 * 如果指定了显式构造函数参数值，则应用此函数，将所有剩余参数与bean工厂中的bean匹配
	 * 
	 * @param beanName
	 * @param mbd
	 * @param ctors
	 * @param args
	 * @return
	 */
	protected BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mbd, Constructor<?>[] ctors,
			Object[] args) {
		return new ConstructorResolver(this).autowireConstructor(beanName, mbd, ctors, args);
	}

	protected BeanWrapper instantiateBean(String beanName, RootBeanDefinition mbd) {
		Object instantiate = this.instantiationStrategy.instantiate(mbd, beanName, super.parentBeanFactory);
		BeanWrapper bw = new BeanWrapperImpl(instantiate);
		return bw;
	}

	protected BeanWrapper instantiateUsingFactoryMethod(String beanName, RootBeanDefinition mbd, Object[] args) {
		return new ConstructorResolver(this).instantiateUsingFactoryMethod(beanName, mbd, args);
	}

	@Override
	public Object initializeBean(Object bean, String beanName) throws BeansException {
		return this.initializeBean(beanName, bean, null);
	}

	protected Object initializeBean(String beanName, Object bean, RootBeanDefinition mbd) {
		/**
		 * 若Bean实现了Aware接口，那么根据Bean的实例类型能够感知到自身的如下属性
		 * BeanNameAware：调用setBeanName方法，感知BeanName
		 * BeanClassLoaderAware：调用setBeanClassLoader方法，感知BeanClassLoader
		 * BeanFactoryAware：调用setBeanFactory方法，感知BeanFactory
		 */
		invokeAwareMethods(beanName, bean);

		// beanForPostProcessor为经过bean后处理器处理的bean，此处防止变量命名冲突特改为此名
		return this.initializeBean(bean, beanName, mbd, beanForPostProcessor -> {
			return invokeInitMethods(beanName, beanForPostProcessor, mbd);
		});
	}

	/**
	 * 调用当前Bean的init方法
	 * 
	 * @param beanName
	 * @param bean
	 * @param mbd
	 * @return
	 */
	protected Object invokeInitMethods(String beanName, Object bean, RootBeanDefinition mbd) {
		boolean isInitializingBean = (bean instanceof InitializingBean);
		if (isInitializingBean) {
			// 调用InitializingBean接口的afterPropertiesSet()方法
			try {
				((InitializingBean) bean).afterPropertiesSet();
			} catch (Exception e) {
				logger.warn("调用InitializingBean实现类的 ‘afterPropertiesSet()’方法抛出异常，by bean：" + beanName);
				e.printStackTrace();
			}
		}

		// 在此调用自定义的初始化bean方法
		if (mbd != null && bean.getClass() != NullBean.class) {
			Set<String> initMethodNames = mbd.getInitMethods();
			mbd.isNonPublicAccessAllowed();
			Class<? extends Object> clz = bean.getClass();
			for (String initMethod : initMethodNames) {
				if (Assert.hasText(initMethod)) {
					// 若实现了InitializingBean接口则跳过
					if (isInitializingBean && initMethod.equals("afterPropertiesSet")) {
						continue;
					}
					try {
						Method method = clz.getMethod(initMethod);
						method.invoke(bean);
					} catch (Exception e) {
						logger.warn("调用InitializingBean实现类的 ‘" + initMethod + "()’方法抛出异常，by bean：" + beanName);
						e.printStackTrace();
					}
				}
			}
		}
		return bean;
	}

	protected Object initializeBean(Object bean, String beanName, RootBeanDefinition mbd,
			ActiveFunction<Object, Object> function) {
		bean = applyBeanPostProcessorsBeforeInitialization(bean, mbd);
		try {
			bean = function.active(bean);
		} catch (Throwable e) {
			throw new BeanCreationException("调用init方法失败，by bean：" + beanName, e);
		}
		bean = applyBeanPostProcessorsAfterInitialization(bean, mbd);
		DebugUtils.log(logger, "实例初始化完成，by name：" + beanName);
		return bean;
	}

	protected void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper bw) {
		DebugUtils.log(logger, "开始进行实例初始化，by name：" + beanName);
		if (!this.applyBeanPostProcessorsAfterInstantiation(bw.getWrappedInstance(), mbd)) {
			return;
		}

		/**
		 * 本实现不使用属性编辑器和属性描述符操作属性
		 */
//		PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);

//		MutablePropertyValues newPvs = null;
//		// 若此bean为根据name或type进行自动注入
//		switch (mbd.getResolvedAutowireMode()) {
//			case AutowireUtils.AUTOWIRE_BY_NAME  :
//				// 拷贝RootBeanDefinition持有的需要注入的属性集，而不直接对其进行修改
//				newPvs = new MutablePropertyValues(pvs);
//				autowireByName(beanName, mbd, bw, newPvs);
//				
//			case AutowireUtils.AUTOWIRE_BY_TYPE    :
//				newPvs = new MutablePropertyValues(pvs);
//				autowireByType(beanName, mbd, bw, newPvs);
//		}
//		if (newPvs != null) {
//			pvs = newPvs;
//		}

		this.applyBeanPostProcessorsProperties(mbd.getPropertyValues(), bw.getWrappedInstance(), mbd);

		// Spring 在此将PropertyValues对象保存到BeanWarpper实现类中，但本实现不使用PropertyValues进行属性注入
	}

	protected void invokeAwareMethods(String beanName, Object bean) {
		if (bean instanceof Aware) {
			if (bean instanceof BeanNameAware) {
				((BeanNameAware) bean).setBeanName(beanName);
			}
			if (bean instanceof BeanClassLoaderAware) {
				ClassLoader bcl = getBeanClassLoader();
				if (bcl != null) {
					((BeanClassLoaderAware) bean).setBeanClassLoader(bcl);
				}
			}
			if (bean instanceof BeanFactoryAware) {
				((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
			}
//			if (bean instanceof EnvironmentAware) {
//				((EnvironmentAware) bean).setEnvironment(environment);
//			}
		}
	}

	@Override
	public Object resolveBeanByName(String name, DependencyDescriptor descriptor) throws BeansException {
		InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
		try {
			return getBean(name, descriptor.getSourceClass());
		} finally {
			ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
		}
	}

	/**
	 * 立即解析依赖项，返回合适的bean对象
	 * 
	 * @param descriptor
	 * @param requestingBeanName
	 * @param autowiredBeanNames
	 * @return
	 */
	protected abstract Object doResolveDependency(DependencyDescriptor descriptor, String requestingBeanName,
			Set<String> autowiredBeanNames);

	protected abstract Object resolveBean(ResolvableType forRawClass, Object[] args);

	protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
		if (super.hasSmartInstantiationAwareBeanPostProcessor) {
			for (SmartInstantiationAwareBeanPostProcessor ibp : super.smartInstantiationList) {
				bean = ibp.getEarlyBeanReference(bean, mbd);
			}
		}
		return bean;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T createBean(Class<T> beanClass) throws BeansException {
		// 使用原型bean定义，避免将bean注册为依赖bean
		RootBeanDefinition bd = new RootBeanDefinition(beanClass);
		bd.setScope(ScopeUtils.SCOPE_PROTOTYPE);
		return (T) createBean(beanClass.getName(), bd, null);
	}

	@Override
	public Object createBean(Class<?> beanClass, int autowireMode) throws BeansException {
		// 使用非单例bean定义，避免将bean注册为依赖bean。
		RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode);
		bd.setScope(ScopeUtils.SCOPE_PROTOTYPE);
		return createBean(beanClass.getName(), bd, null);
	}

	@Override
	public Object autowire(Class<?> beanClass, int autowireMode) throws BeansException {
		// 使用非单例bean定义，避免将bean注册为依赖bean。
		final RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode);
		bd.setScope(ScopeUtils.SCOPE_PROTOTYPE);
		if (bd.getResolvedAutowireMode() == AutowireUtils.AUTOWIRE_CONSTRUCTOR) {
			return autowireConstructor(beanClass.getName(), bd, null, null).getWrappedInstance();
		} else {
			Object instantiate = this.instantiationStrategy.instantiate(bd, null, super.parentBeanFactory);
			populateBean(beanClass.getName(), bd, new BeanWrapperImpl(instantiate));
			return instantiate;
		}
	}

	@Override
	public void autowireBean(Object existingBean) throws BeansException {
		RootBeanDefinition bd = new RootBeanDefinition(ClassUtils.getUserClass(existingBean));
		bd.setScope(ScopeUtils.SCOPE_PROTOTYPE);
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		populateBean(bd.getBeanClass().getName(), bd, bw);
	}

	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return getBean(requiredType, (Object[]) null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
		Assert.notNull(requiredType, "requiredType 不能为null");
		Object resolved = resolveBean(ResolvableType.forClass(requiredType), args);
		if (resolved == null) {
			throw new NoSuchBeanDefinitionException(requiredType);
		}
		return (T) resolved;
	}

	@Override
	public Object configureBean(Object existingBean, String beanName) throws BeansException {
		this.earlySingletonObjects.put(beanName, existingBean);
		RootBeanDefinition mbd = getBeanDefinition(beanName);
		RootBeanDefinition bd = null;
		if (bd == null) {
			bd = new RootBeanDefinition(mbd);
		}
		if (!bd.isPrototype()) {
			bd.setScope(ScopeUtils.SCOPE_PROTOTYPE);
		}
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		populateBean(beanName, bd, bw);
		return initializeBean(beanName, existingBean, bd);
	}

	@Override
	protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
		return applyBeanPostProcessorsAfterInitialization(object, this.getBeanDefinition(beanName));
	}

	@Override
	public void autowireBeanProperties(Object existingBean, int autowireMode) throws BeansException {
		if (autowireMode == AutowireUtils.AUTOWIRE_CONSTRUCTOR) {
			throw new IllegalArgumentException("现有bean实例不支持'AUTOWIRE_CONSTRUCTOR'");
		}
		RootBeanDefinition bd = new RootBeanDefinition(ClassUtils.getUserClass(existingBean), autowireMode);
		bd.setScope(ScopeUtils.SCOPE_PROTOTYPE);
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		populateBean(bd.getBeanClass().getName(), bd, bw);

	}

	@Override
	public void destroyBean(Object existingBean) {
		try {
			new DisposableBeanAdapter(existingBean, super.destructionList).destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected InstantiationStrategy getInstantiationStrategy() {
		return this.instantiationStrategy;
	}

	public ParameterNameDiscoverer getParameterNameDiscoverer() {
		return this.pnd;
	}

	public void setAutowireCandidateResolver(AutowireCandidateResolver autowireCandidateResolver) {
		this.autowireCandidateResolver = autowireCandidateResolver;
	}

	public abstract AutowireCandidateResolver getAutowireCandidateResolver(AbstractAutowireCapableBeanFactory beanFactory);

	// -------------------------------------------------旧方法--------------------------------------------

	/**
	 * 根据属性类型注入
	 * 
	 * @param beanName
	 * @param mbd
	 * @param bw
	 * @param newPvs
	 */
	@SuppressWarnings("unused")
	private void autowireByType(String beanName, RootBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues newPvs) {
	}

	/**
	 * 根据属性名注入
	 * 
	 * @param beanName
	 * @param mbd
	 * @param bw
	 * @param newPvs
	 */
	@SuppressWarnings("unused")
	private void autowireByName(String beanName, RootBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues newPvs) {
	}
}

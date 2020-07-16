package org.zy.fluorite.beans.factory.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.exception.BeanCreationException;
import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.Scope;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.DestructionAwareBeanPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.InstantiationAwareBeanPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.MergedBeanDefinitionPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.SmartInstantiationAwareBeanPostProcessor;
import org.zy.fluorite.beans.factory.utils.BeanFactoryUtils;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.convert.SimpleConversionServiceStrategy;
import org.zy.fluorite.core.exception.BeanIsAbstractException;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.BeanExpressionResolver;
import org.zy.fluorite.core.interfaces.ConversionServiceStrategy;
import org.zy.fluorite.core.interfaces.StringValueResolver;
import org.zy.fluorite.core.interfaces.instantiation.DisposableBean;
import org.zy.fluorite.core.interfaces.instantiation.FactoryBean;
import org.zy.fluorite.core.interfaces.instantiation.SmartFactoryBean;
import org.zy.fluorite.core.subject.NamedThreadLocal;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.ScopeUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月8日 下午4:44:51;
 * @Description
 */
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {

	/** 父Bean工厂实现 */
	protected BeanFactory parentBeanFactory;

	/**  */
	protected ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	/** Bean表达式解析策略 */
	protected BeanExpressionResolver beanExpressionResolver;

//	protected ConfigurableEnvironment environment;
	
	protected ConversionServiceStrategy conversionServiceStrategy = new SimpleConversionServiceStrategy();

	/** 要应用的字符串解析器，例如用于注解属性值解析 */
	protected final List<StringValueResolver> embeddedValueResolvers = new CopyOnWriteArrayList<>();

	/** 应用用于Bean创建的Bean后处理器 */
	protected final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();

	/** 标志当前BeanFactory是否已注册 {@linkplain InstantiationAwareBeanPostProcessors} 实现 */
	protected volatile boolean hasInstantiationAwareBeanPostProcessors;

	/** 标志当前BeanFactory是否已注册 {@linkplain DestructionAwareBeanPostProcessors} 实现 */
	protected volatile boolean hasDestructionAwareBeanPostProcessors;
	
	/** 标志当前BeanFactory是否已注册 {@linkplain MergedBeanDefinitionPostProcessors} 实现 */
	protected volatile boolean hasMergedBeanDefinitionPostProcessors;
	
	/** 标志当前BeanFactory是否已注册 {@linkplain SmartInstantiationAwareBeanPostProcessor} 实现 */
	protected volatile boolean hasSmartInstantiationAwareBeanPostProcessor;

	/** 从作用域标识符字符串映射到相应的作用域. */
	protected final Map<String, Scope> scopes = new LinkedHashMap<>(8);

	/** beanName : 合并的 RootBeanDefinition */
//	protected final Map<String, RootBeanDefinition> localBeanDefinitions = new ConcurrentHashMap<>(256);

	/** 已创建至少一次的beanName. */
	protected final Set<String> alreadyCreated = Collections.newSetFromMap(new ConcurrentHashMap<>(256));

	/** 当前正在创建的bean的名称集合 */
	protected final ThreadLocal<Object> prototypesCurrentlyInCreation = new NamedThreadLocal<>("当前正在创建的bean的名称集合");

	// 在第一次调用如下泛型的BeanPostProcessor实现类时填充如下集合中
	protected final List<DestructionAwareBeanPostProcessor> destructionList = new CopyOnWriteArrayList<>();
	protected final List<MergedBeanDefinitionPostProcessor> mergedList = new CopyOnWriteArrayList<>();
	protected final List<InstantiationAwareBeanPostProcessor> instantiationList = new CopyOnWriteArrayList<>();
	protected final List<SmartInstantiationAwareBeanPostProcessor> smartInstantiationList = new CopyOnWriteArrayList<>();
	
	// 保证所有类型的后处理器的类型效验
	@Deprecated
	protected int destructiotSize ;
	@Deprecated
	protected int mergedSize;
	@Deprecated
	protected int instantiationSize;
	@Deprecated
	protected int smarrtInstantiationSize;

	public AbstractBeanFactory() {
		super();
	}

	/**
	 * 判断当前是否处于启动注册阶段，true则处于
	 * @return
	 */
	protected boolean hasBeanCreationStarted() {
		return !this.alreadyCreated.isEmpty();
	}
	
	public AbstractBeanFactory(BeanFactory parentBeanFactory) {
		super();
		this.parentBeanFactory = parentBeanFactory;
	}

	@Override
	public ConversionServiceStrategy getConversionServiceStrategy() {
		return this.conversionServiceStrategy;
	}
	
	@Override
	public BeanFactory getParentBeanFactory() {
		return this.parentBeanFactory;
	}

	@Override
	public boolean containsLocalBean(String name) {
		String beanName = transformedBeanName(name);
		return ( (containsSingleton(beanName) || containsBeanDefinition(beanName)) &&
				(!BeanFactoryUtils.isFactoryBeanInstance(name)) );
	}

	@Override
	public boolean containsBean(String name) {
		String beanName = transformedBeanName(name);
		if (containsSingleton(beanName) || containsBeanDefinition(beanName)) {
			return (!BeanFactoryUtils.isFactoryBeanInstance(name) || isFactoryBean(name));
		}
		BeanFactory parentBeanFactory = getParentBeanFactory();
		return (parentBeanFactory != null && parentBeanFactory.containsBean(name));
	}
	
	/**
	 * 创建初始化Bean对象
	 * @param beanName
	 * @param mbd
	 * @param args
	 * @return
	 * @throws BeanCreationException
	 */
	protected abstract Object createBean(String beanName, RootBeanDefinition mbd, Object[] args)	throws BeanCreationException;
	
	@Override
	public Object getBean(String name) throws BeansException {
		return doGetBean(name, null, null, false);
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return doGetBean(name, requiredType, null, false);
	}

	@Override
	public Object getBean(String name, Object... args) throws BeansException {
		return doGetBean(name, null, args, false);
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType, Object... args) throws BeansException {
		return doGetBean(name, requiredType, args, false);
	}

	@SuppressWarnings("unchecked")
	protected <T> T doGetBean(final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly) throws BeansException {
		// 字符串剔除，必要时去掉工厂解引用引用，并进行别名解析
		final String beanName = transformedBeanName(name);
		Object bean;

		/** 急切地检查单例缓存中是否有手动注册的单例	 */
		Object sharedInstance = getSingleton(beanName);
		if (sharedInstance != null && args == null) {
			// 如有需要隐式的注册依赖Bean，同时返回创建的Bean对象
			bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
		} else {
			// 检查父工厂中是否存在bean定义
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				if (parentBeanFactory instanceof AbstractBeanFactory) {
					return ((AbstractBeanFactory) parentBeanFactory).doGetBean( name, requiredType, args, typeCheckOnly);
				} else if (args != null) {
					// 使用显式参数委派到父级.
					return (T) parentBeanFactory.getBean(name, args);
				} else {
					// 没有参数->委托给标准getBean方法
					return parentBeanFactory.getBean(name, requiredType);
				}
			}

			try {
				/** 根据beanName获得对应的RootBeanDefinition实例 */
				final RootBeanDefinition mbd = getBeanDefinition(beanName);
				// 检查给定的RootBeanDefinition的abstractFlag属性，若是抽象的则引发验证异常
				if (mbd.isAbstract()) {
					throw new BeanIsAbstractException(beanName);
				}

				// 保证当前bean所依赖的bean的初始化.
				String[] dependsOn = mbd.getDependsOn(); // 返回此bean所依赖的bean名称
				if (dependsOn != null) {
					for (String dep : dependsOn) {
						if (isDependent(beanName, dep)) { 
							throw new BeanCreationException(mbd.getDescription());
						}
						// 注册依赖Bean
						registerDependentBean(dep, beanName);
						try {
							// 实例化被依赖Bean，调用方法：doGetBean(name, null, null, false);
							getBean(dep);
						} catch (NoSuchBeanDefinitionException ex) {
							throw new BeanCreationException(mbd.getDescription());
						}
					}
				}

				/**
				 * 创建bean实例.
				 * 检查RootBeanDefinition的scope属性是否为SCOPE_SINGLETON【“singleton”】或SCOPE_DEFAULT【“”】
				 */
				if (mbd.isSingleton()) {
					sharedInstance = getSingleton(beanName, () -> {
						try {
							return createBean(beanName, mbd, args);
						} catch (BeansException ex) {
							/*
							 * 显式地从单例缓存中移除实例：创建过程可能急切地将实例放在那里，以允许循环引用解析。
							 * 同时移除接收到对该bean的临时引用的任何bean。
							 */
							destroySingleton(beanName);
							throw ex;
						}
					});
					// 检查Bean是否为FactoryBean实现，是则调用其getObject方法获得beanName对应的实例对象，然后为其应用相关后处理器。反之则返回原参数sharedInstance
					bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
				} else if (mbd.isPrototype()) {
					// 它是一个原型->创建一个新实例.
					Object prototypeInstance = null;
					try {
						// 将当前Bean标记为正在创建的原型对象
						beforePrototypeCreation(beanName);
						prototypeInstance = createBean(beanName, mbd, args);
					} finally {
						// 将当前Bean取消标记为正在创建的原型对象
						afterPrototypeCreation(beanName);
					}
					bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
				} else {
					String scopeName = mbd.getScope();
					final Scope scope = this.scopes.get(scopeName);
					if (scope == null) {
						throw new IllegalStateException("没有为作用域名称注册作用域 '" + scopeName + "'");
					}
					try {
						// 调用对应的作用域对象创建当前Bean
						Object scopedInstance = scope.get(beanName, () -> {
							// 将当前Bean标记为正在创建的原型对象
							beforePrototypeCreation(beanName);
							try {
								return createBean(beanName, mbd, args);
							} finally {
								// 将当前Bean取消标记为正在创建的原型对象
								afterPrototypeCreation(beanName);
							}
						});
						bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
					} catch (IllegalStateException ex) {
						throw new BeanCreationException("");
					}
				}
			} catch (BeansException ex) {
				cleanupAfterBeanCreationFailure(beanName);
				throw ex;
			}
		}
		return (T) bean;
	}

	private String transformedBeanName(String name) {
		return canonicalName(BeanFactoryUtils.transformedBeanName(name));
	}

	protected Object getObjectForBeanInstance(Object beanInstance, String name, String beanName, RootBeanDefinition mbd) {
		if (BeanFactoryUtils.isFactoryBeanInstance(name)) { // 若BeanName以“&”为前缀则为true
			if (beanInstance instanceof NullBean) {
				return beanInstance;
			}
			Assert.isTrue( beanInstance instanceof FactoryBean , "beanName前缀 ‘&’ 赋予了非FactoryBean实例的Bean");

			/**
			 * 此处方法为SpringBoot 2.2.7.RELEASE 新增
			 * 对于BeanName以“&”为前缀的Bean，在此直接返回。
			 * 方便SmartFactoryBean接口的isEagerInit方法控制调用getObject()方法的时机
			 */
			if (mbd != null) {
				mbd.setFactoryBean(true);
			}
			return beanInstance;
		}

		if (!(beanInstance instanceof FactoryBean)) { // 若当前bean为工厂bean，则此判断为false
			return beanInstance;
		}
		// 执行到此则表示需要调用FactoryBean实现的getObject方法进行实例化
		if (mbd != null) {
			mbd.setFactoryBean(true);
		}
		// 从FactoryBean缓存中获得对应的FactoryBena实例
		Object object = getCachedObjectForFactoryBean(beanName);
		if (object == null) {
			FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
			object = getObjectFromFactoryBean(factory, beanName);
		}
		return object;
	}
	
	/**
	 * 在bean创建失败后，对缓存的元数据执行适当的清理
	 * @param beanName
	 */
	protected void cleanupAfterBeanCreationFailure(String beanName) {
		synchronized (getBeanDefinitions()) {
			this.alreadyCreated.remove(beanName);
		}
	}
	
	protected boolean isPrototypeCurrentlyInCreation(String beanName) {
		Object curVal = this.prototypesCurrentlyInCreation.get();
		return (curVal != null &&
				(curVal.equals(beanName) || (curVal instanceof Set && ((Set<?>) curVal).contains(beanName))));
	}

	@SuppressWarnings("unchecked")
	protected void beforePrototypeCreation(String beanName) {
		Object curVal = this.prototypesCurrentlyInCreation.get();
		if (curVal == null) {
			this.prototypesCurrentlyInCreation.set(beanName);
		}
		else if (curVal instanceof String) {
			Set<String> beanNameSet = new HashSet<>(2);
			beanNameSet.add((String) curVal);
			beanNameSet.add(beanName);
			this.prototypesCurrentlyInCreation.set(beanNameSet);
		}
		else {
			Set<String> beanNameSet = (Set<String>) curVal;
			beanNameSet.add(beanName);
		}
	}

	@SuppressWarnings("unchecked")
	protected void afterPrototypeCreation(String beanName) {
		Object curVal = this.prototypesCurrentlyInCreation.get();
		if (curVal instanceof String) {
			this.prototypesCurrentlyInCreation.remove();
		}
		else if (curVal instanceof Set) {
			Set<String> beanNameSet = (Set<String>) curVal;
			beanNameSet.remove(beanName);
			if (beanNameSet.isEmpty()) {
				this.prototypesCurrentlyInCreation.remove();
			}
		}
	}
	
	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		Object beanInstance = getSingletonForObject(beanName, false);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean) {
				return ((FactoryBean<?>) beanInstance).isSingleton();
			}
			return true;
		}

		BeanFactory parentBeanFactory = getParentBeanFactory();
		if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
			return parentBeanFactory.isSingleton(name);
		}

		RootBeanDefinition mbd = getBeanDefinition(beanName);

		if (mbd.isSingleton()) {
			if (mbd.isFactoryBean()) {
				if (BeanFactoryUtils.isFactoryBeanInstance(name)) {
					return true;
				}
				FactoryBean<?> factoryBean = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
				return factoryBean.isSingleton();
			}
			return true;
		}else {
			return false;
		}
	}

	@Override
	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);
		
		BeanFactory parentBeanFactory = getParentBeanFactory();
		if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
			return parentBeanFactory.isPrototype(name);
		}

		RootBeanDefinition mbd = getBeanDefinition(beanName);
		if (mbd.isPrototype()) {
			return true;
		}
		
		if (isFactoryBean(beanName, mbd)) {
			final FactoryBean<?> fb = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
			return ((fb instanceof SmartFactoryBean && ((SmartFactoryBean<?>) fb).isPrototype()) ||
						!fb.isSingleton());
		} else {
			return false;
		}
	}

	@Override
	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		return getType(name, true);
	}
	
	@Override
	public Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		Object beanInstance = getSingletonForObject(beanName, false);
		if (beanInstance != null && beanInstance.getClass() != NullBean.class) {
			if (beanInstance instanceof FactoryBean && !BeanFactoryUtils.isFactoryBeanInstance(name)) {
				return ((FactoryBean<?>) beanInstance).getObjectType();
			}
			else {
				return beanInstance.getClass();
			}
		}

		RootBeanDefinition mbd = getBeanDefinition(beanName);
		Class<?> beanClass = mbd.getBeanClass();
		if (mbd != null && !BeanFactoryUtils.isFactoryBeanInstance(name)) {
			if (beanClass != null && !FactoryBean.class.isAssignableFrom(beanClass)) {
				if (! FactoryBean.class.isAssignableFrom(beanClass)) { // 不是FactoryBean子类则直接返回
					return beanClass;
				} else if (allowFactoryBeanInit && mbd.isSingleton()) { 
					// 是FactoryBean子类则创建此FactoryBean实例调用其getObjectType()方法
					FactoryBean<?> factoryBean = doGetBean(FACTORY_BEAN_PREFIX + beanName, FactoryBean.class, null, true);
				
					Assert.notNull(factoryBean,"指定bean名称的创建FactoryBean实例为null，by beanName："+beanName);
					return 	factoryBean.getObjectType();
				}
			}
		}
		return null;
	}

	@Override
	public void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException {
		this.parentBeanFactory = parentBeanFactory;
	}

	@Override
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	@Override
	public ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	@Override
	public void setBeanExpressionResolver(BeanExpressionResolver resolver) {
		this.beanExpressionResolver = resolver;
	}

	@Override
	public BeanExpressionResolver getBeanExpressionResolver() {
		return this.beanExpressionResolver;
	}

	@Override
	public void addEmbeddedValueResolver(StringValueResolver valueResolver) {
		this.embeddedValueResolvers.add(valueResolver);
	}

	@Override
	public boolean hasEmbeddedValueResolver() {
		return !this.embeddedValueResolvers.isEmpty();
	}

	@Override
	public String resolveEmbeddedValue(String value) {
		if (value == null) {
			return null;
		}
		String result = null;
		for (StringValueResolver resolver : this.embeddedValueResolvers) {
			result = resolver.resolveStringValue(value);
			if (result == null) {
				return null;
			}
		}
		return result;
	}

	@Override
	public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
		Assert.notNull(beanPostProcessor, "BeanPostProcessor must not be null");
		this.beanPostProcessors.remove(beanPostProcessor);
		this.beanPostProcessors.add(beanPostProcessor);
		
		if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
			this.hasInstantiationAwareBeanPostProcessors = true;
			InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) beanPostProcessor;
			this.instantiationList.remove(ibp);
			this.instantiationList.add(ibp);
			
			if (beanPostProcessor instanceof SmartInstantiationAwareBeanPostProcessor) {
				this.hasSmartInstantiationAwareBeanPostProcessor = true;
				SmartInstantiationAwareBeanPostProcessor sibp = (SmartInstantiationAwareBeanPostProcessor) beanPostProcessor;
				this.smartInstantiationList.remove(sibp);
				this.smartInstantiationList.add(sibp);
			}
		}
		
		if (beanPostProcessor instanceof MergedBeanDefinitionPostProcessor) {
			this.hasMergedBeanDefinitionPostProcessors = true;
			MergedBeanDefinitionPostProcessor mbp = (MergedBeanDefinitionPostProcessor) beanPostProcessor;
			this.mergedList.remove(mbp);
			this.mergedList.add(mbp);
		}
		
		if (beanPostProcessor instanceof DestructionAwareBeanPostProcessor) {
			this.hasDestructionAwareBeanPostProcessors = true;
			DestructionAwareBeanPostProcessor dbp = (DestructionAwareBeanPostProcessor) beanPostProcessor;
			this.destructionList.remove(dbp);
			this.destructionList.add(dbp);
		}
		
		
	}

	@Override
	public void registerScope(String scopeName, Scope scope) {
		Assert.hasText(scopeName, "Scope 名称不能为null");
		Assert.notNull(scope, "Scope对象不能为null");
		if (ScopeUtils.SCOPE_SINGLETON.equals(scopeName) || ScopeUtils.SCOPE_PROTOTYPE.equals(scopeName)) {
			throw new IllegalArgumentException("无法替换现有  'singleton' 和 'prototype' 作用域");
		}
		Scope previous = this.scopes.put(scopeName, scope);
		if (DebugUtils.debug) {
			if ((previous != null && previous != scope) ) {
				logger.info("作用域 {} 替换为 {}，by scope：{}" ,previous, scope, scopeName);
			}else {
				logger.info("注册作用域："+scope);
			}
		}
	}

	@Override
	public String[] getRegisteredScopeNames() {
		return StringUtils.toStringArray(this.scopes.keySet());
	}

	@Override
	public Scope getRegisteredScope(String scopeName) {
		Assert.hasText(scopeName, "Scope名称不能为null或空串");
		return this.scopes.get(scopeName);
	}

	@Override
	public void copyConfigurationFrom(AbstractBeanFactory otherFactory) {
		Assert.notNull(otherFactory, "BeanFactory must not be null");
		setBeanClassLoader(otherFactory.getBeanClassLoader());
		setBeanExpressionResolver(otherFactory.getBeanExpressionResolver());
		if (otherFactory instanceof AbstractBeanFactory) {
			AbstractBeanFactory otherAbstractFactory = (AbstractBeanFactory) otherFactory;
			for (BeanPostProcessor bp : otherAbstractFactory.beanPostProcessors) {
				addBeanPostProcessor(bp);
			}
			this.scopes.putAll(otherAbstractFactory.scopes);
		}
		else {
			String[] otherScopeNames = otherFactory.getRegisteredScopeNames();
			for (String scopeName : otherScopeNames) {
				this.scopes.put(scopeName, otherFactory.getRegisteredScope(scopeName));
			}
		}
	}

	@Override
	public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);
		Object beanInstance = getSingletonForObject(beanName, false);
		if (beanInstance != null) {
			return (beanInstance instanceof FactoryBean);
		}
		if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory) {
			return ((ConfigurableBeanFactory) getParentBeanFactory()).isFactoryBean(name);
		}
		return isFactoryBean(beanName, getBeanDefinition(beanName));
	}

	protected boolean isFactoryBean(String beanName, RootBeanDefinition mbd) {
		Boolean result = mbd.isFactoryBean();
		if (!result) {
			Class<?> beanType = mbd.getBeanClass();
			result = (beanType != null && FactoryBean.class.isAssignableFrom(beanType));
			mbd.setFactoryBean(result);
		}
		return result;
	}
	
	@Override
	public String[] getDependentBeans(String beanName) {
		return StringUtils.toStringArray(super.dependentBeanMap.get(beanName));
	}

	@Override
	public String[] getDependenciesForBean(String beanName) {
		return StringUtils.toStringArray(super.dependenciesForBeanMap.get(beanName));
	}

	@Override
	public void destroyBean(String beanName, Object beanInstance) {
		destroyBean(beanName, beanInstance, getBeanDefinition(beanName));
	}

	private void destroyBean(String beanName, Object beanInstance,RootBeanDefinition mdb) {
		if (beanInstance instanceof DisposableBean) {
			super.destroyBean(beanName, (DisposableBean) beanInstance);
		} else {
			if (!this.beanPostProcessors.isEmpty() && this.hasDestructionAwareBeanPostProcessors) {
				for (DestructionAwareBeanPostProcessor processor : this.destructionList) {
					processor.postProcessBeforeDestruction(beanInstance, mdb);
				}
			}
		}
	}

	@Override
	public void destroyScopedBean(String beanName) {
		RootBeanDefinition mbd = getBeanDefinition(beanName);
		if (mbd.isSingleton() || mbd.isPrototype()) {
			throw new IllegalArgumentException("销毁的作用域与可变作用域中的对象不对应，by beanName："+beanName);
		}
		String scopeName = mbd.getScope();
		Scope scope = this.scopes.get(scopeName);
		if (scope == null) {
			throw new IllegalStateException("注册的作用域对象中没有相关联的作用域对象，by： " + scopeName);
		}
		Object bean = scope.remove(beanName);
		if (bean != null) {
			destroyBean(beanName, bean, mbd);
		}
	}
	
	@Override
	public void destroySingletons() {
		super.destroySingletons();
	}
	
	@Override
	public boolean isTypeMatch(String name, Class<?> typeToMatch) {
		return this.isTypeMatch(name, ResolvableType.forClass(typeToMatch), true);
	}
	
	@Override
	public boolean isTypeMatch(String name, ResolvableType typeToMatch) {
		return this.isTypeMatch(name, typeToMatch, true);
	}
	
	/**
	 * 检查具有给定名称的bean是否与指定类型匹配
	 * @param beanName
	 * @param type
	 * @param allowEagerInit - 是否急切的实例化对象
	 * @return
	 */
	@Override
	public boolean isTypeMatch(String name, ResolvableType typeToMatch, boolean allowEagerInit) {
		String beanName = transformedBeanName(name);
		BeanDefinition beanDefinition = getBeanDefinition(beanName);
		boolean isFactoryBean = this.isFactoryBean(name);
		
		Class<?> objectType = null;
		Object beanInstance = getSingleton(beanName);
		if (beanInstance != null) {
			if (isFactoryBean) {
				objectType = ((FactoryBean<?>) beanInstance).getObjectType();
				/**
				 * isAssignableFrom
				 * isInstance
				 */
				return typeToMatch.isAssignableFrom(objectType);
			} else {
				objectType = beanInstance.getClass();
				return typeToMatch.isAssignableFrom(objectType);
			}
		} else {
			if ( this.isCurrentlyInCreation(beanName)) { // 跳过当前正在创建的bean
				return false;
			}
			
			if (allowEagerInit) { // 若允许则实例化FactoryBean实例
				if (isFactoryBean) {
					beanInstance = getBean(beanName);
					return typeToMatch.isAssignableFrom(beanInstance.getClass());
				}
			} 
			Class<?> beanClass = beanDefinition.getBeanClass();
			// 不允许则忽略FactoryBean实例创建的Bean
			return isFactoryBean ? false : typeToMatch.isAssignableFrom(beanClass);
		}
	}
	
	@Override
	public int getBeanPostProcessorCount() {
		return this.beanPostProcessors.size();
	}

	public boolean hasInstantiationAwareBeanPostProcessors() {
		return this.hasInstantiationAwareBeanPostProcessors;
	}
	public boolean hasDestructionAwareBeanPostProcessors() {
		return this.hasDestructionAwareBeanPostProcessors;
	}
	protected Logger getLogger() {
		return logger;
	}
//	public Environment getEnvironment() {
//		return environment;
//	}
//	public void setEnvironment(ConfigurableEnvironment environment) {
//		this.environment = environment;
//	}
}

package org.zy.fluorite.aop.autoproxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.aop.interfaces.AdvisorAdapterRegistry;
import org.zy.fluorite.aop.interfaces.AopInfrastructureBean;
import org.zy.fluorite.aop.interfaces.Pointcut;
import org.zy.fluorite.aop.interfaces.TargetSource;
import org.zy.fluorite.aop.interfaces.function.TargetSourceCreator;
import org.zy.fluorite.aop.proxy.ProxyFactory;
import org.zy.fluorite.aop.support.ProxyProcessorSupport;
import org.zy.fluorite.aop.support.adapter.GlobalAdvisorAdapterRegistry;
import org.zy.fluorite.aop.target.SingletonTargetSource;
import org.zy.fluorite.aop.utils.AopProxyUtils;
import org.zy.fluorite.beans.factory.aware.BeanFactoryAware;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.processor.SmartInstantiationAwareBeanPostProcessor;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.instantiation.FactoryBean;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @DateTime 2020年7月4日 下午6:02:57;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public abstract class AbstractAutoProxyCreator extends ProxyProcessorSupport	implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected ConfigurableListableBeanFactory beanFactory;
	
	/**  存储被代理对象的beanName */
	private final Set<String> targetSourcedBeans = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

	/** 存储早期代理引用，也就是cacheKey */
	private final Map<Object, Object> earlyProxyReferences = new ConcurrentHashMap<>(16);
	
	/** 保存切点代理类映射 */
	private final Map<Object, Class<?>> proxyTypes = new ConcurrentHashMap<>(16);
	
	/** 根据BeanName保存其指代的Bean是否需要切面织入的boolean值 */
	private final Map<Object, Boolean> advisedBeans = new ConcurrentHashMap<>(256);

	/** 子类的便利常数：没有附加拦截器的代理，只是普通的”的返回值 */
	protected static final Object[] PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS = new Object[0];

	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

	/** 指示是否应冻结代理。从super重写以防止配置过早冻结	 */
	private boolean freezeProxy = false;

	/** 默认情况下没有通用的拦截器 */
	private String[] interceptorNames = new String[0];

	/** 是否首先应用公共拦截器 */
	private boolean applyCommonInterceptorsFirst = true;

	private TargetSourceCreator[] customTargetSourceCreators;
	
	@Override
	public Object getEarlyBeanReference(Object bean, BeanDefinition definition) throws BeansException {
		Object cacheKey = getCacheKey(bean.getClass(), definition.getBeanName());
		this.earlyProxyReferences.put(cacheKey, bean);
		return wrapIfNecessary(bean, definition, cacheKey);
	}

	@Override
	public Object postProcessBeforeInstantiation(String beanName, BeanDefinition definition) throws BeansException {
		Class<?> beanClass = definition.getBeanClass();
		Object cacheKey = getCacheKey(beanClass, beanName);

		if (Assert.hasText(beanName) || !this.targetSourcedBeans.contains(beanName)) {
			if (this.advisedBeans.containsKey(cacheKey)) {
				return null;
			}
			
			if (isInfrastructureClass(beanClass,definition.getAnnotationMetadata() ) || shouldSkip(beanClass, beanName)) {
				this.advisedBeans.put(cacheKey, Boolean.FALSE);
				return null;
			}
		}

		TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
		if (targetSource != null) {
			if (Assert.hasText(beanName)) {
				this.targetSourcedBeans.add(beanName);
			}
			Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
			Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
			this.proxyTypes.put(cacheKey, proxy.getClass());
			return proxy;
		}
		return null;
	}

	/** 如果子类将bean标识为要代理的bean，则使用配置的拦截器创建一个代理 */
	@Override
	public Object postProcessAfterInitialization(Object bean, BeanDefinition mbd) throws BeansException {
		if (bean != null) {
			Object cacheKey = getCacheKey(mbd.getBeanClass(), mbd.getBeanName());
			if (this.earlyProxyReferences.remove(cacheKey) != bean) {
				return wrapIfNecessary(bean, mbd, cacheKey);
			}
		}
		return bean;
	}

	@Override
	public Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
		if (this.proxyTypes.isEmpty()) {
			return null;
		}
		Object cacheKey = getCacheKey(beanClass, beanName);
		return this.proxyTypes.get(cacheKey);
	}

	/** 为给定的bean类和bean名称构建一个缓存键 */
	protected Object getCacheKey(Class<?> beanClass, String beanName) {
		if (Assert.hasText(beanName)) {
			return (FactoryBean.class.isAssignableFrom(beanClass) ? 
					BeanFactory.FACTORY_BEAN_PREFIX + beanName	: beanName);
		} else {
			return beanClass;
		}
	}

	@Override
	public void setFrozen(boolean frozen) {
		this.freezeProxy = frozen;
	}

	@Override
	public boolean isFrozen() {
		return this.freezeProxy;
	}

	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	/**
	 * 设置自定义的TargetSourceCreators实现。
	 * 如果列表为空，或者它们都返回null，将为每个bean创建一个{@link SingletonTargetSource}
	 */
	public void setCustomTargetSourceCreators(TargetSourceCreator... targetSourceCreators) {
		this.customTargetSourceCreators = targetSourceCreators;
	}

	/** 设置来自于BeanFactory的公共拦截器bean名称，若未设置此项则不会对bean应用公共拦截器 */
	public void setInterceptorNames(String... interceptorNames) {
		this.interceptorNames = interceptorNames;
	}

	public void setApplyCommonInterceptorsFirst(boolean applyCommonInterceptorsFirst) {
		this.applyCommonInterceptorsFirst = applyCommonInterceptorsFirst;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		Assert.isTrue(beanFactory instanceof ConfigurableListableBeanFactory , "感知到的BeanFactory对象未实现ConfigurableListableBeanFactory接口");
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	protected BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	protected Object wrapIfNecessary(Object bean, BeanDefinition definition, Object cacheKey) {
		String beanName = definition.getBeanName();
		if (Assert.hasText(beanName) && this.targetSourcedBeans.contains(beanName)) {
			return bean;
		}
		if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
			return bean;
		}
		if (isInfrastructureClass(bean.getClass() , definition.getAnnotationMetadata()) || shouldSkip(bean.getClass(), beanName)) {
			this.advisedBeans.put(cacheKey, false);
			return bean;
		}

		// 获得当前Bean所适用的所有拦截器集
		Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
		if (specificInterceptors != null) {
			this.advisedBeans.put(cacheKey, true);
			Object proxy = createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
			this.proxyTypes.put(cacheKey, proxy.getClass());
			return proxy;
		}

		this.advisedBeans.put(cacheKey, false);
		return bean;
	}
	
	/**
	 * 为给定的bean创建一个AOP代理。 
	 * @param clz
	 * @param beanName
	 * @param interceptors - 指定给这个bean的拦截器集，可以是一个空集但不能为null
	 * @param targetSource - 代理的TargetSource
	 * @return
	 */
	protected Object createProxy(Class<? extends Object> beanClass, String beanName, Object[] interceptors , TargetSource targetSource) {
		if (this.beanFactory instanceof ConfigurableListableBeanFactory) {
			// 在当前Bean的BeanDefinition的Attribute中保存beanClass属性
			AopProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName, beanClass);
		}

		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.copyFrom(this);

		if (!proxyFactory.isProxyTargetClass()) {
			if (shouldProxyTargetClass(beanClass, beanName)) {
				proxyFactory.setProxyTargetClass(true);
			}
			else {
				evaluateProxyInterfaces(beanClass, proxyFactory);
			}
		}
		
		Advisor[] advisors = buildAdvisors(beanName, interceptors);
		proxyFactory.addAdvisors(advisors);
		proxyFactory.setTargetSource(targetSource);

		// 空方法
		customizeProxyFactory(proxyFactory);

		// freezeProxy = false
		proxyFactory.setFrozen(this.freezeProxy);
		if (advisorsPreFiltered()) { // 设置是否预筛选此代理配置，以便它仅包含适用的advise（与此代理的目标类匹配）。[true]
			proxyFactory.setPreFiltered(true);
		}
		return proxyFactory.getProxy();
	}

	/**
	 * 确定给定的bean是否应该用它的目标类而不是它的接口来代理。
	 * 检查相应bean定义的“preserveTargetClass”属性。
	 * @param beanClass
	 * @param beanName
	 * @return
	 */
	protected boolean shouldProxyTargetClass(Class<? extends Object> beanClass, String beanName) {
		return (this.beanFactory instanceof ConfigurableListableBeanFactory &&
				AopProxyUtils.shouldProxyTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName));
	}

	/**
	 * 确定给定bean的Advisor，包括特定的拦截器和通用的拦截器，所有这些都与Advisor接口相适应。
	 * @param beanName
	 * @param interceptors - 特定于此bean的拦截器集（可以为空集，但不能为null）
	 * @return 给定bean的Advisor列表
	 */
	protected Advisor[] buildAdvisors(String beanName, Object[] interceptors) {
		Advisor[] commonInterceptors = resolveInterceptorNames();

		List<Object> allInterceptors = new ArrayList<>();
		if (interceptors != null) {
			allInterceptors.addAll(Arrays.asList(interceptors));
			if (commonInterceptors.length > 0) {
				if (this.applyCommonInterceptorsFirst) {
					allInterceptors.addAll(0, Arrays.asList(commonInterceptors));
				} else {
					allInterceptors.addAll(Arrays.asList(commonInterceptors));
				}
			}
		}
		DebugUtils.logFromAop(logger, "使用通用的拦截器和指定的拦截器为当前Bean创建隐式的代理，by bean："+beanName);

		Advisor[] advisors = new Advisor[allInterceptors.size()];
		for (int i = 0; i < allInterceptors.size(); i++) {
			advisors[i] = this.advisorAdapterRegistry.wrap(allInterceptors.get(i));
		}
		return advisors;
	}

	/** 将本类持有的拦截器名称解析为Advisor对象	 */
	protected Advisor[] resolveInterceptorNames() {
		BeanFactory bf = this.beanFactory;
		ConfigurableBeanFactory cbf = (bf instanceof ConfigurableBeanFactory ? (ConfigurableBeanFactory) bf : null);
		List<Advisor> advisors = new ArrayList<>();
		// this.interceptorNames：默认值是没有公共拦截器
		for (String beanName : this.interceptorNames) {
			if (cbf == null || !cbf.isCurrentlyInCreation(beanName)) {
				Assert.isTrue(bf != null, "解析拦截器名称需要BeanFactory ");
				Object next = bf.getBean(beanName);
				advisors.add(this.advisorAdapterRegistry.wrap(next));
			}
		}
		return advisors.toArray(new Advisor[0]);
	}

	
	/** 
	 * 子类返回的advisor是否已预先筛选以匹配bean的目标类，从而允许在为AOP调用构建advisor链时跳过ClassFilter检查。
	 * 默认值为false。如果子类总是返回预先筛选的顾问，则子类可能会覆盖此项
	 * @return
	 */
	protected boolean advisorsPreFiltered() {
		return false;
	}

	/** 子类可以选择实现这一点：例如，更改公开的接口。默认实现为空 */
	protected void customizeProxyFactory(ProxyFactory proxyFactory) {}

	/**
	 * 根据指定bean的类对象和beanName依据自定义实现的TargetSourceCreator对象。
	 * 为给定的bean创建一个特殊的TargetSource（如果有的话）
	 * @param beanClass
	 * @param beanName
	 * @return
	 */
	protected TargetSource getCustomTargetSource(Class<?> beanClass, String beanName) {
		if (this.customTargetSourceCreators != null && this.beanFactory != null && this.beanFactory.containsBean(beanName)) {
			for (TargetSourceCreator creator : this.customTargetSourceCreators) {
				TargetSource ts = creator.getTargetSource(beanClass, beanName);
				if (ts != null) {
					DebugUtils.logFromAop(logger, "TargetSourceCreator [" + creator +	"] 找到的自定义TargetSource，by beanName：" + beanName);
					return ts;
				}
			}
		}
		// 未找到自定义目标源.
		return null;
	}
	
	/**
	 * 返回是否要代理给定的bean，要应用哪些附加advice，例如拦截器和advisor
	 * @param beanClass
	 * @param beanName
	 * @param customTargetSource - getCustomTargetSource方法返回的TargetSource，设置null忽略
	 * @return 特定bean的附加拦截器数组；如果没有附加的拦截器，则为空数组；如果没有其他拦截器，则为空数组；
	 * 		如果根本没有代理，则为null，即使是普通的拦截器也不行
	 * @throws BeansException
	 */
	protected abstract Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName,
			TargetSource customTargetSource) throws BeansException;

	/**
	 * 判断是否跳过给定的bean
	 * @param beanClass
	 * @param beanName
	 * @return 若要跳过则返回true，不跳过则返回false
	 */
	protected boolean shouldSkip(Class<?> beanClass, String beanName) {
		return false;
	}

	/**
	 * 返回给定的bean类是否表示不应代理的基础结构类。
	 * 默认实现将Advice、Advisor和AopInfrastructureBean视为基础结构类
	 * @param beanClass
	 * @param metadata
	 * @return
	 */
	protected boolean isInfrastructureClass(Class<?> beanClass , AnnotationMetadata metadata) {
		//返回给定的bean类是否表示不应代理的基础结构类。默认实现将Advice、Pointcut、Advisor、AopInfrastructureBean视为基础结构类
		boolean flag = Advice.class.isAssignableFrom(beanClass) || Pointcut.class.isAssignableFrom(beanClass) ||
				Advisor.class.isAssignableFrom(beanClass) ||	AopInfrastructureBean.class.isAssignableFrom(beanClass);
		if (flag && logger.isTraceEnabled()) {
			logger.warn("不能自动代理基础结构类：[" + beanClass.getName() + "]");
		}
		return flag;
	}
}

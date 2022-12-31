package org.zy.fluorite.context.support;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.factory.aware.EmbeddedValueResolverAware;
import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.beans.factory.interfaces.AutowireCapableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanFactoryPostProcessor;
import org.zy.fluorite.context.event.ApplicationEvent;
import org.zy.fluorite.context.event.ContextClosedEvent;
import org.zy.fluorite.context.event.ContextRefreshedEvent;
import org.zy.fluorite.context.event.ContextStartedEvent;
import org.zy.fluorite.context.event.ContextStoppedEvent;
import org.zy.fluorite.context.event.PayloadApplicationEvent;
import org.zy.fluorite.context.event.SimpleApplicationEventMulticaster;
import org.zy.fluorite.context.event.interfaces.ApplicationEventMulticaster;
import org.zy.fluorite.context.event.interfaces.ApplicationEventPublisher;
import org.zy.fluorite.context.event.interfaces.ApplicationListener;
import org.zy.fluorite.context.exception.NoSuchMessageException;
import org.zy.fluorite.context.interfaces.ApplicationContext;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;
import org.zy.fluorite.context.interfaces.HierarchicalMessageSource;
import org.zy.fluorite.context.interfaces.LifecycleProcessor;
import org.zy.fluorite.context.interfaces.MessageSource;
import org.zy.fluorite.context.interfaces.MessageSourceResolvable;
import org.zy.fluorite.context.interfaces.aware.ApplicationContextAware;
import org.zy.fluorite.context.interfaces.aware.ApplicationEventPublisherAware;
import org.zy.fluorite.context.interfaces.aware.MessageSourceAware;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.environment.StandardEnvironment;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.EnvironmentAware;
import org.zy.fluorite.core.subject.StandardBeanExpressionResolver;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @DateTime 2020年6月17日 下午2:03:41;
 * @author zy(azurite-Y);
 * @Description
 */
public abstract class AbstractApplicationContext implements ConfigurableApplicationContext {
	protected static final Logger logger = LoggerFactory.getLogger(AbstractApplicationContext.class);

	public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";
	public static final String LIFECYCLE_PROCESSOR_BEAN_NAME = "lifecycleProcessor";
	public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";

	protected String id;

	protected String displayName;

	/** 父类上下文 */
	protected ApplicationContext parent;

	protected ConfigurableEnvironment environment;

	protected final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<>();

	/** 上下文启动时的系统时间（毫秒） */
	protected long startupDate;

	/**
	 * 指示此上下文当前是否处于活动状态的标志 AtomicBoolean - 可以原子更新的布尔值
	 */
	protected final AtomicBoolean active = new AtomicBoolean();

	/** 指示此上下文当前是否处于关闭状态的标志 */
	protected final AtomicBoolean closed = new AtomicBoolean();

	/** “刷新”和“销毁”的同步锁对象 */
	protected final Object startupShutdownMonitor = new Object();

	/** 对JVM关闭回调的引用（如果已注册）。 */
	protected Thread shutdownHook;

	protected LifecycleProcessor lifecycleProcessor;

	protected MessageSource messageSource;

	/** 事件发布中使用的帮助程序类 */
	protected ApplicationEventMulticaster applicationEventMulticaster;

	/** 静态指定的侦听器 */
	protected final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

	/** 在刷新前注册的本地侦听器 */
	protected Set<ApplicationListener<?>> earlyApplicationListeners;

	/** 在ApplicationEventMulticaster创建之前触发的应用程序事件集合 */
	protected Set<ApplicationEvent> earlyApplicationEvents;

	public AbstractApplicationContext() {
	}

	@Override
	public boolean isActive() {
		return this.active.get();
	}

	public AbstractApplicationContext(ApplicationContext parent) {
		this();
		setParent(parent);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		Assert.hasText(displayName, "'displayName'不能为null或空串");
		this.displayName = displayName;
	}

	public ApplicationContext getParent() {
		return parent;
	}

	@Override
	public void setParent(ApplicationContext parent) {
		this.parent = parent;
		if (parent != null) {
			Environment parentEnvironment = parent.getEnvironment();
			if (parentEnvironment instanceof ConfigurableEnvironment) {
				// 将父上下文中的属性源保存到此上下文环境中
				getEnvironment().merge((ConfigurableEnvironment) parentEnvironment);
			}
		}
	}

	public ConfigurableEnvironment getEnvironment() {
		return this.environment = (this.environment == null) ? createEnvironment() : this.environment;
	}

	public ConfigurableEnvironment createEnvironment() {
		return new StandardEnvironment();
	}

	public void setEnvironment(ConfigurableEnvironment environment) {
		this.environment = environment;
	}

	private MessageSource getMessageSource() {
		Assert.notNull(this.messageSource, "MessageSource还未初始化，需调用’refresh‘方法之后才能使用上下文访问消息");
		return this.messageSource;
	}

	/**
	 * 获得上下文持有的MessageSource的父类MessageSource
	 * 
	 * @return
	 */
	protected MessageSource getInternalParentMessageSource() {
		return (getParent() instanceof AbstractApplicationContext
				? ((AbstractApplicationContext) getParent()).messageSource
				: getParent());
	}

	@Override
	public String getApplicationName() {
		return "";
	}

	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		return getBeanFactory();
	}

	@Override
	public long getStartupDate() {
		return this.startupDate;
	}

	@Override
	public void publishEvent(ApplicationEvent event) {
		publishEvent(event, null);
	}

	@Override
	public void publishEvent(Object event) {
		publishEvent(event, null);
	}

	/**
	 * 事件发布，且调用父类上下文之中的事件侦听器
	 * 
	 * @param event
	 * @param eventType
	 */
	protected void publishEvent(Object event, ResolvableType eventType) {
		Assert.notNull(event, "‘Event’不能为null");

		// 如有必要，将事件装饰为ApplicationEvent
		ApplicationEvent applicationEvent;
		if (event instanceof ApplicationEvent) {
			applicationEvent = (ApplicationEvent) event;
		} else {
			applicationEvent = new PayloadApplicationEvent<>(this, event);
			if (eventType == null) {
				eventType = ((PayloadApplicationEvent<?>) applicationEvent).getResolvableType();
			}
		}

		// 如果可能的话，现在就进行多播-或者在初始化多播主机后进行惰性的多播
		if (this.earlyApplicationEvents != null) {
			this.earlyApplicationEvents.add(applicationEvent);
		} else {
			getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
		}

		if (this.parent != null) {
			if (this.parent instanceof AbstractApplicationContext) {
				((AbstractApplicationContext) this.parent).publishEvent(applicationEvent, eventType);
			} else {
				this.parent.publishEvent(applicationEvent);
			}
		}
	}

	ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
		Assert.notNull(this.applicationEventMulticaster,
				"ApplicationEventMulticaster未初始化，在通过上下文多播事件之前需调用'refresh'方法，by id：" + this.id);
		return this.applicationEventMulticaster;
	}

	LifecycleProcessor getLifecycleProcessor() throws IllegalStateException {
		Assert.notNull(this.lifecycleProcessor, "LifecycleProcessor未初始化，在通过上下文多播事件之前需调用'refresh'方法，by：" + this);
		return this.lifecycleProcessor;
	}

	@Override
	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
		Assert.notNull(postProcessor, "‘BeanFactoryPostProcessor'不能为null");
		this.beanFactoryPostProcessors.add(postProcessor);
	}

	public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
		return this.beanFactoryPostProcessors;
	}

	@Override
	public void addApplicationListener(ApplicationListener<?> listener) {
		Assert.notNull(listener, "ApplicationListener must not be null");
		if (this.applicationEventMulticaster != null) {
			this.applicationEventMulticaster.addApplicationListener(listener);
		}
		this.applicationListeners.add(listener);
	}

	public Collection<ApplicationListener<?>> getApplicationListeners() {
		return this.applicationListeners;
	}

	@Override
	public void registerShutdownHook() {
		if (this.shutdownHook == null) {
			this.shutdownHook = new Thread(() -> {
				synchronized (startupShutdownMonitor) {
					doClose();
				}
			}, SHUTDOWN_HOOK_THREAD_NAME);
		}
	}

	@Override
	public void close() {
		synchronized (this.startupShutdownMonitor) {
			doClose();
			if (this.shutdownHook != null) {
				try {
					Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
				} catch (IllegalStateException ex) {
					// 忽略-虚拟机已关闭
				}
			}
		}
	}

	protected void doClose() {
		// 检查是否需要实际关闭
		if (this.active.get() && this.closed.compareAndSet(false, true)) {
			// 若closed为false时替换为true
			if (DebugUtils.debug) {
				logger.info("Closing " + this);
			}

			try {
				// 发布关闭事件
				publishEvent(new ContextClosedEvent(this));
			} catch (Throwable ex) {
				logger.warn("由处理ContextClosedEvent的ApplicationListener所引发的异常", ex);
			}

			// 停止所有生命周期bean，以避免在单个销毁过程中出现延迟
			if (this.lifecycleProcessor != null) {
				try {
					this.lifecycleProcessor.onClose();
				} catch (Throwable ex) {
					logger.warn("上下文关闭时由LifecycleProcessor引发异常", ex);
				}
			}

			// 在上下文的BeanFactory中销毁所有缓存的单例bean
			destroyBeans();

			// 关闭此上下文本身的状态
			closeBeanFactory();

			// 如果子类愿意，让它们做最后的清理
			onClose();

			// 将本地应用程序侦听器重置为预刷新状态
			if (this.earlyApplicationListeners != null) {
				this.applicationListeners.clear();
				this.applicationListeners.addAll(this.earlyApplicationListeners);
			}

			// 切换到非活动状态
			this.active.set(false);
		}
	}

	protected void onClose() {
	}

	protected abstract void closeBeanFactory();

	protected void destroyBeans() {
		getBeanFactory().destroySingletons();
	}

	protected void assertBeanFactoryActive() {
		if (!this.active.get()) { // 上下文处于非活动状态
			if (this.closed.get()) { // 上下文关闭
				throw new IllegalStateException(" 当前上下文已关闭，by：" + getDisplayName());
			} else {
				throw new IllegalStateException(" 当前上下文尚未刷新，by：" + getDisplayName());
			}
		}
	}

	@Override
	public Object getBean(String name) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(name);
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(name, requiredType);
	}

	@Override
	public Object getBean(String name, Object... args) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(name, args);
	}

	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(requiredType);
	}

	@Override
	public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBean(requiredType, args);
	}

	@Override
	public boolean containsBean(String name) {
		return getBeanFactory().containsBean(name);
	}

	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().isSingleton(name);
	}

	@Override
	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().isPrototype(name);
	}

	@Override
	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().getType(name);
	}

	@Override
	public Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
		return getBeanFactory().getType(name, allowFactoryBeanInit);
	}

	@Override
	public String[] getAliases(String name) {
		return getBeanFactory().getAliases(name);
	}

	@Override
	public boolean containsBeanDefinition(String beanName) {
		return getBeanFactory().containsBeanDefinition(beanName);
	}

	@Override
	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}

	@Override
	public List<String> getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForType(type);
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public String[] getBeanNamesForType(Class<?> type) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForType(type);
	}

	@Override
	public String[] getBeanNamesForTypeInclusionSingle(Class<?> type) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForTypeInclusionSingle(type);
	}
	
	@Override
	public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBeansOfType(type);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		assertBeanFactoryActive();
		return getBeanFactory().getBeansOfType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
		assertBeanFactoryActive();
		return getBeanFactory().getBeanNamesForAnnotation(annotationType);
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
			throws BeansException {
		assertBeanFactoryActive();
		return getBeanFactory().getBeansWithAnnotation(annotationType);
	}

	@Override
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().findAnnotationOnBean(beanName, annotationType);
	}

	@Override
	public BeanFactory getParentBeanFactory() {
		return getParent();
	}

	@Override
	public boolean containsLocalBean(String name) {
		return getBeanFactory().containsLocalBean(name);
	}

	/**
	 * 持有BeanFactory实现的父类BeanFactory实现
	 */
	protected BeanFactory getInternalParentBeanFactory() {
		return (getParent() instanceof ConfigurableApplicationContext
				? ((ConfigurableApplicationContext) getParent()).getBeanFactory()
				: getParent());
	}

	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		return getMessageSource().getMessage(code, args, defaultMessage, locale);
	}

	@Override
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(code, args, locale);
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(resolvable, locale);
	}

	@Override
	public void start() {
		getLifecycleProcessor().start();
		publishEvent(new ContextStartedEvent(this));
	}

	/**
	 * 因为上下文实现了Lifecycle接口，且注册到了BeanFactory中，所以生命周期处理器关闭生命周期Bean时也会调用此方法
	 */
	@Override
	public void stop() {
		getLifecycleProcessor().stop();
		publishEvent(new ContextStoppedEvent(this));
	}

	@Override
	public boolean isRunning() {
		return (this.lifecycleProcessor != null && this.lifecycleProcessor.isRunning());
	}

	protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;

	@Override
	public abstract ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;

	@Override
	public void refresh() throws BeansException, IllegalStateException {
		/**
		 * 准备此上下文以进行刷新，重置元数据资源缓存 设置其启动日期和活动标志，以及执行属性源的任何初始化
		 */
		prepareRefresh();
		/**
		 * 告诉子类刷新内部bean工厂，在创建上下文对象的时候已创建的BeanFactory，默认实现为DefaultListableBeanFactory
		 * 为BeanFactory设置序列化ID，此ID是配置文件中的spring.application.name属性
		 */
		ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

		/**
		 * 准备bean工厂以供在此上下文中使用 1.设置加载资源的ClassLoader实现，与热部署有关 - RestartClassLoader
		 * 2.根据ClassLoader实现设置Bean表达式解析器 - StandardBeanExpressionResolver 3.设置Bean后处理器 –
		 * ApplicationContextAwareProcessor 4.设置依赖关系忽略接口
		 * 5.注册依赖。关联BeanFactory、ResourceLoader、ApplicationEventPublisher、ApplicationContext的实现
		 * 6.设置Bean后处理器 –ApplicationListenerDetector
		 * 7.检查预配置的bean别名映射集合，将符合的映射加载为bean，且将映射名保存到已注册的单例bean名称集合中【系统环境、Servlet环境、系统配置】
		 */
		prepareBeanFactory(beanFactory);

		try {
			/**
			 * 允许在上下文子类中对bean工厂进行后处理. 添加WebApplicationContextServletContextAwareProcessor
			 * Bean后处理器
			 */
			postProcessBeanFactory(beanFactory);

			// 调用注册bean工厂后处理器
			invokeBeanFactoryPostProcessors(beanFactory);

			// 注册bean后处理器.
			registerBeanPostProcessors(beanFactory);

			/**
			 * 初始化此上下文的MessageSource(消息源)到bean容器中，
			 * DelegatingMessageSource（使用空消息源可以接受getMessage调用）
			 * 若预配置的bean别名映射集合中无对应的映射(messageSource)则注册DelegatingMessageSource对象到bean容器中
			 */
			initMessageSource();

			// 为此上下文初始化一个事件注册表，若无相关映射(applicationEventMulticaster)则将SimpleApplicationEventMulticaster对象注册的bean容器中
			initApplicationEventMulticaster();

			/**
			 * 初始化特定上下文子类中的其他特殊bean - [ThemeSource、WebService-TomcatWebServer]
			 * 注册ThemeSource实现，若无相关映射则将ResourceBundleThemeSource对象注册到bean容器中
			 * 根据已初始化的WebApplicationContext调用ServletWebServerFactory的getWebServer方法，执行tomcat和mvc框架相关配置
			 */
			onRefresh();

			/**
			 * 检查侦听器bean并注册它们将ApplicationListener集合、 bean容器中的ApplicationListener类型的bean 、
			 * earlyApplicationEvents(早期的事件发布程序)注册到事件注册表中。
			 */
			registerListeners();

			// 实例化所有剩余的（非延迟初始化）单例
			finishBeanFactoryInitialization(beanFactory);

			/** 初始化生命周期处理器并由生命周期处理器启动生命周期Bean，接着发布上下文刷新事件 */
			finishRefresh();
		} catch (BeansException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("上下文初始化期间遇到异常- " + "取消刷新尝试: " + ex);
			}

			// 销毁已创建的单例以避免资源悬空.
			destroyBeans();

			// 重置“活动”标志.
			cancelRefresh(ex);

			// 向调用方传播异常.
			throw ex;
		} finally {
			resetCommonCaches();
		}
	}

	/**
	 * 准备此上下文以进行刷新，重置元数据资源缓存 设置其启动日期和活动标志，以及执行属性源的任何初始化
	 */
	protected void prepareRefresh() {
		this.startupDate = System.currentTimeMillis();
		this.closed.set(false);
		this.active.set(true);

		DebugUtils.log(logger, "应用程序上下文属性，by id：" + this.id);
		// 验证setRequiredProperties指定的每个属性是否存在并解析为一个非空值
		getEnvironment().validateRequiredProperties();

		// 存储预刷新应用程序侦听器
		if (this.earlyApplicationListeners == null) {
			this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
		} else {
			// 将本地应用程序侦听器重置为预刷新状态
			this.applicationListeners.clear();
			this.applicationListeners.addAll(this.earlyApplicationListeners);
		}

		// 允许收集早期的ApplicationEvents，一旦applicationEventMulticaster可用就发布事件
		this.earlyApplicationEvents = new LinkedHashSet<>();
	}

	/**
	 * 告诉子类刷新内部bean工厂，在创建上下文对象的时候已创建的BeanFactory，默认实现为DefaultListableBeanFactory
	 * 为BeanFactory设置序列化ID，此ID是配置文件中的spring.application.name属性
	 */
	protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
		refreshBeanFactory();
		return getBeanFactory();
	}

	/**
	 * 准备bean工厂以供在此上下文中使用 1.设置加载资源的ClassLoader实现，与热部署有关 - RestartClassLoader
	 * 2.根据ClassLoader实现设置Bean表达式解析器 - StandardBeanExpressionResolver 3.设置Bean后处理器 –
	 * ApplicationContextAwareProcessor 4.设置依赖关系忽略接口
	 * 5.注册依赖。关联BeanFactory、ResourceLoader、ApplicationEventPublisher、ApplicationContext的实现
	 * 6.设置Bean后处理器 –ApplicationListenerDetector
	 * 7.检查预配置的bean别名映射集合，将符合的映射加载为bean，且将映射名保存到已注册的单例bean名称集合中【系统环境、Servlet环境、系统配置】
	 */
	protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// 告诉内部bean工厂使用上下文的类加载器
		beanFactory.setBeanClassLoader(ClassUtils.getDefaultClassLoader());
		// 设置BeanName的表达式解析程序
		beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver());

		// 添加一个新的BeanPostProcessor，它将应用于此工厂创建的bean。在工厂配置期间调用
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));

		// 将忽略依赖的接口加入到BeanFactory的ignoredDependencyInterfaces容器中
		beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
		beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
		beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
		/**
		 * BeanFactory接口未在普通工厂中注册为可解析类型。MessageSource已注册（并为自动连线找到）为bean
		 * 注册可解析的依赖项及其实现，保存到BeanFactory的resolvableDependencies容器中，最终会作用于自动装配
		 */
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
		beanFactory.registerResolvableDependency(ApplicationContext.class, this);
		beanFactory.registerResolvableDependency(Environment.class, this.environment);
		/**
		 * 注册早期的Bean后处理器，以便将内部bean检测为应用程序侦听器.
		 * ApplicationListenerDetector：用于检测实现ApplicationListener接口的bean。
		 * 这将捕获getBeanNamesForTypeand相关操作无法可靠检测的bean，这些操作仅对顶级bean起作用
		 *
		 */
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));
		/**
		 * 注册默认环境bean.
		 */
		if (!beanFactory.containsLocalBean(Environment.ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(Environment.ENVIRONMENT_BEAN_NAME, getEnvironment());
		}
		if (!beanFactory.containsLocalBean(Environment.SYSTEM_PROPERTIES)) {
			beanFactory.registerSingleton(Environment.SYSTEM_PROPERTIES, getEnvironment().getSystemProperties());
		}
		if (!beanFactory.containsLocalBean(Environment.SYSTEM_ENVIRONMENT)) {
			beanFactory.registerSingleton(Environment.SYSTEM_ENVIRONMENT, getEnvironment().getSystemEnvironment());
		}

	}

	/**
	 * 允许在上下文子类中对bean工厂进行后处理. 添加WebApplicationContextServletContextAwareProcessor
	 * Bean后处理器
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
	}

	/**
	 * 调用注册bean工厂后处理器
	 * 
	 * @param beanFactory
	 */
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		// 调用应用于BeanFactory的上下文对象持有的BeanFactory后处理器
		PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());
	}

	/**
	 * 注册bean后处理器.
	 * 
	 * @param beanFactory
	 */
	protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
	}

	/**
	 * 初始化此上下文的MessageSource(消息源)到bean容器中，
	 * DelegatingMessageSource（使用空消息源可以接受getMessage调用）
	 * 若预配置的bean别名映射集合中无对应的映射(messageSource)则注册DelegatingMessageSource对象到bean容器中
	 */
	private void initMessageSource() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) { // 未执行
			this.messageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
			// 使消息源知道父消息源.
			if (this.parent != null && this.messageSource instanceof HierarchicalMessageSource) {
				HierarchicalMessageSource hms = (HierarchicalMessageSource) this.messageSource;
				if (hms.getParentMessageSource() == null) {
					// 仅当尚未注册父消息源时，才将父上下文设置为父消息源.
					hms.setParentMessageSource(getInternalParentMessageSource());
				}
			}
			if (DebugUtils.debug) {
				logger.info("使用的MessageSource：" + this.messageSource);
			}
		} else {
			// 使用空消息源可以接受getMessage调用.
//			DelegatingMessageSource dms = new DelegatingMessageSource();
//			dms.setParentMessageSource(getInternalParentMessageSource());
			// 初始化上下文的messageSource引用
//			this.messageSource = dms;
			// 注册单例对象
//			beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
//			if (DebugUtils.debug) {
//				logger.info("找不到MessageSource，by name '" + MESSAGE_SOURCE_BEAN_NAME +"'。使用默认的MessageSource：" + this.messageSource);
//			}
		}

	}

	/**
	 * 为此上下文初始化一个事件注册表，若无相关映射(applicationEventMulticaster)则将SimpleApplicationEventMulticaster对象注册的bean容器中
	 */
	protected void initApplicationEventMulticaster() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) { // 未执行
			this.applicationEventMulticaster = beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
					ApplicationEventMulticaster.class);
			if (logger.isDebugEnabled()) {
				logger.debug("使用 ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
			}
		} else {
			this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
			beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
			DebugUtils.log(logger, "找不到已注册的ApplicationEventMulticaster实现，by name：'"
					+ APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "': 使用默认配置 [" + this.applicationEventMulticaster + "]");
		}

	}

	protected void initLifecycleProcessor() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
			this.lifecycleProcessor = beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
			if (DebugUtils.debug) {
				logger.info("使用LifecycleProcessor：" + this.lifecycleProcessor);
			}
		} else {
			DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
			defaultProcessor.setBeanFactory(beanFactory);
			this.lifecycleProcessor = defaultProcessor;
			beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
			if (DebugUtils.debug) {
				logger.info("未找到名为'" + LIFECYCLE_PROCESSOR_BEAN_NAME + "'的bean, 使用默认的LifecycleProcessor："
						+ this.lifecycleProcessor.getClass().getSimpleName());
			}
		}
	}

	/**
	 * 初始化特定上下文子类中的其他特殊bean - [ThemeSource、WebService-TomcatWebServer]
	 * 注册ThemeSource实现，若无相关映射则将ResourceBundleThemeSource对象注册到bean容器中
	 * 根据已初始化的WebApplicationContext调用ServletWebServerFactory的getWebServer方法，执行tomcat和mvc框架相关配置
	 */
	protected void onRefresh() {
	}

	/**
	 * 检查侦听器bean并注册它们将ApplicationListener集合、 bean容器中的ApplicationListener类型的bean 、
	 * earlyApplicationEvents(早期的事件发布程序)注册到事件注册表中。
	 */
	protected void registerListeners() {
		// 先注册静态指定的侦听器.
		for (ApplicationListener<?> listener : getApplicationListeners()) {
			getApplicationEventMulticaster().addApplicationListener(listener);
		}

		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
		for (String listenerBeanName : listenerBeanNames) {
			getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
		}

		// 发布早期的应用程序事件现在我们终于有了一个多主机...
		Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
		this.earlyApplicationEvents = null;
		if (earlyEventsToProcess != null) {
			for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
				getApplicationEventMulticaster().multicastEvent(earlyEvent);
			}
		}

	}

	/**
	 * 实例化所有剩余的（非延迟初始化）单例
	 * 
	 * @param beanFactory
	 */
	protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		// 冻结所有bean定义，表示已注册的bean定义将不再被修改或后处理，允许缓存所有bean定义元数据，不需要进一步更改
		beanFactory.freezeConfiguration();

		// 实例化所有剩余的（非延迟初始化）单例
		beanFactory.preInstantiateSingletons();
	}

	/**
	 * 初始化生命周期处理器并由生命周期处理器启动生命周期Bean，接着发布上下文刷新事件
	 */
	protected void finishRefresh() {
		// 为此上下文初始化生命周期处理器.
		initLifecycleProcessor();

		// 首先将刷新传播到生命周期处理器，由生命周期处理器启动生命周期Bean
		getLifecycleProcessor().onRefresh();

		// 发布最终事件.
		publishEvent(new ContextRefreshedEvent(this));
	}

	/**
	 * 清除元数据缓存，主要清除Utils之中的缓存
	 */
	protected void resetCommonCaches() {
	}

	@Override
	public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().isTypeMatch(name, typeToMatch);
	}

	@Override
	public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
		assertBeanFactoryActive();
		return getBeanFactory().isTypeMatch(name, typeToMatch);
	}

	@Override
	public boolean isTypeMatch(String name, ResolvableType typeToMatch, boolean allowEagerInit) {
		assertBeanFactoryActive();
		return getBeanFactory().isTypeMatch(name, typeToMatch, allowEagerInit);
	}

	protected void cancelRefresh(BeansException ex) {
		this.active.set(false);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getId());
		sb.append(", started on ").append(new Date(getStartupDate()));
		ApplicationContext parent = getParent();
		if (parent != null) {
			sb.append(", parent: ").append(parent.getDisplayName());
		}
		return sb.toString();
	}
}

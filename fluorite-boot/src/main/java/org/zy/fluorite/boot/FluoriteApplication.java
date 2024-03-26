package org.zy.fluorite.boot;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.autoconfigure.web.servlet.AnnotationConfigServletWebServerApplicationContext;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.utils.BeanUtils;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.beans.support.AnnotationAwareOrderComparator;
import org.zy.fluorite.beans.support.AnnotationMetadataHolder;
import org.zy.fluorite.boot.annotation.RunnerAs;
import org.zy.fluorite.boot.context.event.listener.EventPublishingRunListener;
import org.zy.fluorite.boot.interfaces.Banner;
import org.zy.fluorite.boot.interfaces.Banner.Mode;
import org.zy.fluorite.boot.interfaces.FluoriteApplicationRunListener;
import org.zy.fluorite.context.event.interfaces.ApplicationListener;
import org.zy.fluorite.context.interfaces.ApplicationContextInitializer;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;
import org.zy.fluorite.context.support.AbstractApplicationContext;
import org.zy.fluorite.context.support.AnnotationBeanNameGenerator;
import org.zy.fluorite.context.support.ConfigurationClassPostProcessor;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.environment.FactoriesProperty;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.exception.FluoriteRuntimeException;
import org.zy.fluorite.core.io.MetaFileLoader;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.FailureReporterUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;
import org.zy.fluorite.web.context.support.StandardServletEnvironment;

/**
 * ConfigurationWarningsApplicationContextInitializer
 * @DateTime 2020年6月25日 下午10:52:16;
 * @author zy(azurite-Y);
 * @Description Fluorite-Boot模块的逻辑入口，整个Fluorite框架的启动器。 
 */
public class FluoriteApplication {
	public static final Logger logger = LoggerFactory.getLogger(FluoriteApplication.class);

	private Class<?> mainApplicationClass;

	private Banner banner;

	private Banner.Mode bannerMode = Banner.Mode.CONSOLE;

	private ConfigurableEnvironment environment;

	private boolean registerShutdownHook = true;

	private List<ApplicationContextInitializer<?>> initializers = new ArrayList<>();

	private List<ApplicationListener<?>> listeners = new ArrayList<>();

	private RunnerAs rennerAs;
	
	private AnnotationMetadataHolder annotationMetadata;
	
	public FluoriteApplication(Class<?> primarySources) {
		Assert.notNull(primarySources, "根启动类不能为null");
		this.mainApplicationClass = primarySources;
		this.annotationMetadata = new AnnotationMetadataHolder(mainApplicationClass);
		this.rennerAs = annotationMetadata.getAnnotationForClass(RunnerAs.class);
		if (rennerAs == null) {
			String errMsg = "未找到标注@RunnerAs注解的所标注的类，by："+this.mainApplicationClass;
			logger.error(errMsg);
			FailureReporterUtils.report(FluoriteRuntimeException.class , errMsg);
		}
		
		if (this.rennerAs.debug()) {
			DebugUtils.debug = true;
		}
		if (this.rennerAs.debugFormAop()) {
			DebugUtils.debugFromAop = true;
		}
		if (this.rennerAs.debugFromTransaction()) {
			DebugUtils.debugFromTransaction = true;
		}
		
		this.initializers = getInitializers();
		this.listeners = getListeners();
	}

	/**
	 * 
	 * @param primarySource - 根启动类Class对象
	 * @param args          - 根启动类main方法参数
	 * @return
	 */
	public static ConfigurableApplicationContext run(Class<?> primarySources, String[] args) {
		return new FluoriteApplication(primarySources).run(args);
	}

	private ConfigurableApplicationContext run(String[] args) {
		ConfigurableApplicationContext context = null;
		FluoriteApplicationRunListener listeners = getRunListeners(args);
		// 调用多个已注册FluoriteApplicationRunListener的starting()方法 EventPublishingRunListener
		listeners.starting();
		try {
			// 准备环境
			ConfigurableEnvironment environment = prepareEnvironment(listeners, args);

			// 在控制台打印log及版本信息
			printBanner(environment);

			// 创建ApplicationContext。
			context = new AnnotationConfigServletWebServerApplicationContext();

			// 根据配置环境参数填充上下文
			prepareContext(context, environment, listeners, args);

			// 刷新上下文
			refreshContext(context);

			// 执行的是空方法
			afterRefresh(context, args);

			listeners.started(context);
		} catch (Throwable ex) {
			throw new IllegalStateException(ex);
		}

		try {
			listeners.running(context);
		} catch (Throwable ex) {
			throw new IllegalStateException(ex);
		}
		return context;
	}

	/**
	 * 应用程序上下文刷新之后的回调逻辑
	 * @param context
	 * @param args
	 */
	protected void afterRefresh(ConfigurableApplicationContext context, String[] args) {}

	/**
	 * 刷新上下文
	 * @param context
	 */
	private void refreshContext(ConfigurableApplicationContext context) {
		Assert.isInstanceOf(AbstractApplicationContext.class, context,"");
		((AbstractApplicationContext) context).refresh();

		if (this.registerShutdownHook) {
			try {
				context.registerShutdownHook();
			} catch (AccessControlException ex) {}
		}
	}

	private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment2,
			FluoriteApplicationRunListener listeners, String[] args) {
		context.setEnvironment(environment);
		
		// 在上下文刷新之前，将任何ApplicationContextInitializers应用于该上下文
		applyInitializers(context);

		listeners.contextPrepared(context);

		if (this.banner != null) {
			// 1.7.3 将日志打印的Banner实现类对象注册为单例对象，且保存循化引用
			context.getBeanFactory().registerSingleton("fluoriteBootBanner", this.banner);
		}

		context.addBeanFactoryPostProcessor(new ConfigurationClassPostProcessor(environment));
		
		// 为根启动类生成BeanDefinition对象，并读取根启动类的注解信息填充其相关属性
		load(context);
		listeners.contextLoaded(context);
	}

	/**
	 * 为根启动类生成BeanDefinition对象，并读取根启动类的注解信息填充其相关属性
	 * @param context
	 * @param sourceClass
	 */
	private void load(ConfigurableApplicationContext context) {
		BeanDefinition beanDefinition = new RootBeanDefinition(this.mainApplicationClass);
		beanDefinition.setAnnotationMetadata(this.annotationMetadata);
		
		String beanName = rennerAs.value();

		Assert.isInstanceOf(BeanDefinitionRegistry.class, context, "未实现BeanDefinitionRegistry接口的应用程序上下文对象，by："+context.getClass().getName());
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context;
		if (beanName.isEmpty()) {
			beanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(beanDefinition, registry);
		}
		beanDefinition.setBeanName(beanName);

		BeanUtils.processCommonDefinitionAnnotations(beanDefinition, this.annotationMetadata);

		DebugUtils.log(logger, "根启动类BeanDefinition注册，by name：" +beanName);
		registry.registerBeanDefinition(beanName, beanDefinition);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void applyInitializers(ConfigurableApplicationContext context) {
		for (ApplicationContextInitializer initializer : getInitializers()) {
			// ApplicationContextInitializer的子类判断，返回本初始化的上下文类型，其实是ApplicationContextInitializer接口的泛型
			Class<?> genericType = ResolvableType.forClass(initializer.getClass()).as(ApplicationContextInitializer.class).getGenericToClass();

			// 若context不是requiredType的子实现类则抛出异常
			Assert.isInstanceOf(genericType, context, "不支持的ApplicationContextInitializer实现，泛型约束所触发的异常，by generic："+genericType);

			// 初始化给定的应用程序上下文
			DebugUtils.log(logger, "应用程序上下文初始化调用："+initializer.getClass().getName());

			initializer.initialize(context);
		}
	}

	private void printBanner(ConfigurableEnvironment environment2) {
		if (this.bannerMode == Mode.OFF) {
			return;
		}
		this.banner = new FluoriteBootBanner();
		if (this.bannerMode == Mode.CONSOLE) {
			try {
				banner.printBanner(environment, this.mainApplicationClass, System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private ConfigurableEnvironment prepareEnvironment(FluoriteApplicationRunListener listeners, String[] args) {
		ConfigurableEnvironment environment = getOrCreateEnvironment();
		configureEnvironment(environment, args);
		listeners.environmentPrepared(environment);
		return environment;
	}

	private void configureEnvironment(ConfigurableEnvironment environment, String[] args) {
		// 配置数据源
		environment.customizePropertySources();
		environment.setActiveProfiles(environment.getActiveProfiles());
	}

	private ConfigurableEnvironment getOrCreateEnvironment() {
		if (this.environment != null) {
			return this.environment;
		}
		return this.environment = new StandardServletEnvironment();
	}

	private FluoriteApplicationRunListener getRunListeners(String[] args) {
		return new EventPublishingRunListener(this, args);
	}

	@SuppressWarnings("unchecked")
	public List<ApplicationContextInitializer<?>> getInitializers() {
		if (this.initializers.isEmpty()) {
			List<String> loadFactories = MetaFileLoader.loadFactories("fluorite-boot", "fluorite.factories.",
					FactoriesProperty.APPLICATION_CNTEXT_INITIALIZER);
			
			for (String clzName : loadFactories) {
				DebugUtils.log(logger, "从'fluorite.factories'文件中读取到的[ApplicationContextInitializer]预置项："+clzName);
				
				Class<ApplicationContextInitializer<?>> forName = (Class<ApplicationContextInitializer<?>>) ReflectionUtils.forName(clzName);
				if (forName == null) {
					logger.error("从fluorite-boot模块下的‘META-INFO/fluorite.factories文件中载入预设的ApplicationContextInitializer实现类失败。"
							+ "by key：" +FactoriesProperty.APPLICATION_CNTEXT_INITIALIZER+ " value：" + clzName);
				}
				ApplicationContextInitializer<?> applicationListener = ReflectionUtils.instantiateClass(forName);
				this.initializers.add(applicationListener);
			}
			this.initializers.sort(AnnotationAwareOrderComparator.INSTANCE);
		}

		return Collections.unmodifiableList(this.initializers);
	}

	@SuppressWarnings("unchecked")
	public List<ApplicationListener<?>> getListeners() {
		if (this.listeners.isEmpty()) {
			// 读取文件中的预置项
			List<String> loadFactories = MetaFileLoader.loadFactories("fluorite-boot", "fluorite.factories.",
					FactoriesProperty.APPLICATION_LISTENER);
			for (String clzName : loadFactories) {
				DebugUtils.log(logger, "从'fluorite.factories'文件中读取到的[ApplicationListener]预置项："+clzName);
				
				Class<ApplicationListener<?>> forName = (Class<ApplicationListener<?>>) ReflectionUtils.forName(clzName);
				if (forName == null) {
					logger.error("从fluorite-boot模块下的‘META-INFO/fluorite.factories文件中载入预设的ApplicationListener实现类失败。"
							+ "by key：" +FactoriesProperty.APPLICATION_LISTENER+ " value：" + clzName);
				}
				ApplicationListener<?> applicationListener = ReflectionUtils.instantiateClass(forName);
				this.listeners.add(applicationListener);
			}

			this.listeners.sort(AnnotationAwareOrderComparator.INSTANCE);

		}
		return Collections.unmodifiableList(this.listeners);
	}
}

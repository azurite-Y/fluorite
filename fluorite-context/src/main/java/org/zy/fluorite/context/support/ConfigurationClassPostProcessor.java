package org.zy.fluorite.context.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.factory.aware.BeanClassLoaderAware;
import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.BeanNameGenerator;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanDefinitionRegistryPostProcessor;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.context.annotation.ConditionEvaluator;
import org.zy.fluorite.context.annotation.ConfigurationClass;
import org.zy.fluorite.context.annotation.ConfigurationClassBeanDefinitionReader;
import org.zy.fluorite.context.annotation.ConfigurationClassParser;
import org.zy.fluorite.context.utils.ConfigurationClassUtils;
import org.zy.fluorite.core.environment.StandardEnvironment;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.interfaces.EnvironmentAware;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.interfaces.PriorityOrdered;
import org.zy.fluorite.core.utils.AnnotationUtils;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @DateTime 2020年6月19日 下午3:34:41;
 * @author zy(azurite-Y);
 * @Description 负责组件注解解析的后处理器
 */
public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered, BeanClassLoaderAware, EnvironmentAware {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static BeanNameGenerator importBeanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;
	
	private Environment environment;

	private ConditionEvaluator conditionEvaluator;
	
	private ConfigurationClassBeanDefinitionReader reader;
	
	@SuppressWarnings("unused")
	private ClassLoader classLoader;
	
	private final Set<Integer> registriesPostProcessed = new HashSet<>();

	private final Set<Integer> factoriesPostProcessed = new HashSet<>();


	public ConfigurationClassPostProcessor(ConfigurableEnvironment environment) {
		this.environment = environment;
	}

	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		this.environment = environment;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
	
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
		int registryId = System.identityHashCode(registry);
		if (this.registriesPostProcessed.contains(registryId)) {
			throw new IllegalStateException("已对此后处理器调用postProcessBeanDefinitionRegistry，by：" + registry);
		}
		if (this.factoriesPostProcessed.contains(registryId)) {
			throw new IllegalStateException("此后处理器上已针对调用了postProcessBeanFactory，by：" + registry);
		}
		this.registriesPostProcessed.add(registryId);

		processConfigBeanDefinitions(registry);
	}
	
	public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {
		DebugUtils.log(logger, "=调用processConfigBeanDefinitions(BeanDefinitionRegistry)=");
		List<BeanDefinition> configCandidates = new ArrayList<>();
		// 获得已生成BeanDefinition的BeanName
		List<String> candidateNames = registry.getBeanDefinitionNames();
		
		for (String beanName : candidateNames) {
			BeanDefinition beanDef = registry.getBeanDefinition(beanName);
			if (ConfigurationClassUtils.isConfigurationClass(beanDef)) {
				if (DebugUtils.debug) {
					logger.debug("Bean定义已作为配置类处理：" + beanDef);
				}
			} else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef)) {
				// 最终一定会保存根启动类的BeanDefinition对象
				configCandidates.add(beanDef);
			}
		}

		// 如果找不到给定的注解类，则立即返回
		if (configCandidates.isEmpty()) {
			return;
		}

		//按先前确定的@order值排序（如果适用）
		configCandidates.sort((bd1, bd2) -> {
			int i1 = AnnotationUtils.findOrderFromAnnotation(bd1);
			int i2 = AnnotationUtils.findOrderFromAnnotation(bd2);
			return Integer.compare(i1, i2);
		});

		if (this.environment == null) {
			this.environment = new StandardEnvironment();
		}
		
		if (this.conditionEvaluator == null) {
			this.conditionEvaluator = new ConditionEvaluator(registry,environment);;
		}

		// 解析每个@Configuration类
		ConfigurationClassParser parser = new ConfigurationClassParser(this.environment,registry,this.conditionEvaluator);

		Set<BeanDefinition> candidates = new LinkedHashSet<>(configCandidates);
		Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());
		do {
			// 解析在启动过程之中创建的BeanDefinition
			parser.parse(candidates);
			/**
			 * 验证配置类和Bean方法的有效性
			 * 配置类不能被final修饰
			 * 静态@Bean方法没有要验证的约束
			 * @Bean 标记的方法不能为静态、final或private
			 */
			parser.validate(logger);

			Set<ConfigurationClass> configClasses = new LinkedHashSet<>(parser.getConfigurationClasses());
			configClasses.removeAll(alreadyParsed);

			/**
			 * 阅读模型并基于其内容创建BeanDefinition
			 * 根据模型ConfigClass提供的Bean信息填充到BeanDefinition对象和BeanFactory对象中，然后将BeanDefinition对象注册为单例BeanDefinition
			 */
			if (this.reader == null) {
				this.reader = new ConfigurationClassBeanDefinitionReader(registry, this.environment, importBeanNameGenerator , this.conditionEvaluator);
			}
			// 阅读特定的ConfigurationClass，为类本身及其所有bean方法注册bean定义
			this.reader.loadBeanDefinitions(configClasses);
			alreadyParsed.addAll(configClasses);

			candidates.clear();
			if (registry.getBeanDefinitionCount() > candidateNames.size()) {
				List<String> newCandidateNames = registry.getBeanDefinitionNames();
				Set<String> oldCandidateNames = new HashSet<>(candidateNames);
				Set<String> alreadyParsedClasses = new HashSet<>();
				for (ConfigurationClass configurationClass : alreadyParsed) {
					alreadyParsedClasses.add(configurationClass.getClassName());
				}
				for (String candidateName : newCandidateNames) {
					if (!oldCandidateNames.contains(candidateName)) {
						BeanDefinition bd = registry.getBeanDefinition(candidateName);
						// 确保所有注册的BeanDefinition已全部解析，若存在为解析的BeanDefinition则包装为BeanDefinitionHolder对象重新进行解析
						if (ConfigurationClassUtils.checkConfigurationClassCandidate(bd) && !alreadyParsedClasses.contains(bd.getBeanClass().getName())) {
							candidates.add(bd);
						}
					}
				}
				candidateNames = newCandidateNames;
			}
		}
		while (!candidates.isEmpty());
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		DebugUtils.log(logger, "=调用postProcessBeanFactory(ConfigurableListableBeanFactory)=");
		int factoryId = System.identityHashCode(beanFactory);
		if (this.factoriesPostProcessed.contains(factoryId)) {
			throw new IllegalStateException("此后处理器已针对调用了postProcessBeanFactory()方法，by：" + beanFactory);
		}
		this.factoriesPostProcessed.add(factoryId);
		if (!this.registriesPostProcessed.contains(factoryId)) {
			processConfigBeanDefinitions((BeanDefinitionRegistry) beanFactory);
		}
		
		// 使用Cglib代理配置类
		enhanceConfigurationClasses(beanFactory);
//		beanFactory.addBeanPostProcessor(new ImportAwareBeanPostProcessor(beanFactory));
	}

	public void enhanceConfigurationClasses(ConfigurableListableBeanFactory beanFactory) {
		// TODO 自动生成的方法存根
		
	}
}

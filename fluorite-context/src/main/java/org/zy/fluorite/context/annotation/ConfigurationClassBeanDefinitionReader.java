package org.zy.fluorite.context.annotation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.BeanNameGenerator;
import org.zy.fluorite.beans.factory.support.BeanMethod;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.beans.support.AnnotationMetadataHolder;
import org.zy.fluorite.context.annotation.interfaces.ImportBeanDefinitionRegistrar;
import org.zy.fluorite.context.annotation.interfaces.ConfigurationCondition.ConfigurationPhase;
import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年6月24日 下午1:35:52;
 * @author zy(azurite-Y);
 * @Description 读取给定的ConfigurationClass实例集，根据其内容向给定的BeanDefinitionRegistry注册beandefinitions。
 */
public class ConfigurationClassBeanDefinitionReader {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final BeanDefinitionRegistry registry;

	@SuppressWarnings("unused")
	private final Environment environment;

	private final BeanNameGenerator importBeanNameGenerator;

	private ConditionEvaluator conditionEvaluator;
	
	public ConfigurationClassBeanDefinitionReader(BeanDefinitionRegistry registry, Environment environment
			, BeanNameGenerator importBeanNameGenerator, ConditionEvaluator conditionEvaluator) {
		this.registry = registry;
		this.environment = environment;
		this.importBeanNameGenerator = importBeanNameGenerator;
		this.conditionEvaluator = conditionEvaluator;
	}

	public void loadBeanDefinitions(Set<ConfigurationClass> configClasses) {
		for (ConfigurationClass configurationClass : configClasses) {
			loadBeanDefinitionsForConfigurationClass(configurationClass);
		}
	}

	private void loadBeanDefinitionsForConfigurationClass(ConfigurationClass configurationClass) {
		// 在此处理BeanDefinition注册阶段要使用的从属于类的条件注解 [ ConfigurationPhase.REGISTER_BEAN ]
		if (conditionEvaluator.shouldSkip(configurationClass)) {
			String beanName = configurationClass.getBeanName();
			if (Assert.hasText(beanName) && this.registry.containsBeanDefinition(beanName)) {
				this.registry.removeBeanDefinition(beanName);
			}
			return;
		}
		
		BeanDefinition beanDefinition = loadBeanDefinitionByConfigurationClass(configurationClass);
		
		for (BeanMethod beanMethod : configurationClass.getBeanMethods()) {
			loadBeanDefinitionsForBeanMethod(configurationClass,beanMethod,beanDefinition);
		}

//		loadBeanDefinitionsFromImportedResources(configurationClass.getImportedResources());
		loadBeanDefinitionsFromRegistrars(configurationClass.getImportBeanDefinitionRegistrars());
		
		String beanName = beanDefinition.getBeanName();
		RootBeanDefinition definition = this.registry.getBeanDefinition(beanName);
		if (definition == null) {
			this.registry.registerBeanDefinition(beanName, beanDefinition);
		}
	}

	/**
	 * 根据configurationClass中的属性创建BeanDefinition并设置基本属性。若configurationClass中已有则直接返回。<br/>
	 * 此方法针对的是@Import注解导入和自动配置加载的类
	 * @param configurationClass
	 * @return
	 */
	private BeanDefinition loadBeanDefinitionByConfigurationClass(ConfigurationClass configurationClass) {
		BeanDefinition beanDefinition = configurationClass.getBeanDefinition();
		if (beanDefinition != null) {
			return beanDefinition;
		}
		beanDefinition = new RootBeanDefinition(configurationClass.getSource());
		beanDefinition.setAnnotationMetadata(configurationClass.getAnnotationMetadata());
		
		String beanName = configurationClass.getBeanName();
		if (!Assert.hasText(beanName)) {
			beanName = this.importBeanNameGenerator.generateBeanName(beanDefinition, this.registry);
		}
		beanDefinition.setBeanName(beanName);
		return beanDefinition;
		
	}

	/**
	 * 迭代调用ImportBeanDefinitionRegistrar的registerBeanDefinitions方法，手动注册BeanDefinition
	 * @param map
	 * @see {@link ImportBeanDefinitionRegistrar#registerBeanDefinitions(Class, BeanDefinitionRegistry) }
	 */
	private void loadBeanDefinitionsFromRegistrars(Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> registrars) {
		registrars.forEach((registrar, metadata) ->
				registrar.registerBeanDefinitions(metadata, this.registry, this.importBeanNameGenerator));
	}

	/**
	 * 将@Bean标注的方法所代表的的BeanDefinition注册到BeanFactory中
	 * @param configurationClass
	 * @param beanMethod
	 * @param beanDefinition
	 */
	private void loadBeanDefinitionsForBeanMethod(ConfigurationClass configurationClass, BeanMethod beanMethod, BeanDefinition beanDefinition) {
		Method method = beanMethod.getMethod();
		AnnotationMetadata annotationMetadata = configurationClass.getAnnotationMetadata();
		AnnotationAttributes methodAttribute = annotationMetadata.getAnnotationAttributesForMethod(method);
		
		// 在此处理BeanDefinition注册阶段要使用的从属于方法的条件注解 [ ConfigurationPhase.REGISTER_BEAN ]
		if (conditionEvaluator.shouldSkip(methodAttribute,ConfigurationPhase.REGISTER_BEAN)) {
			return ;
		}
		
		
		// 执行到此的Method一定是标注了@Bena注解，且条件允许的
		String factoryBeanName = beanDefinition.getBeanName();
		
		Bean bean = methodAttribute.getAnnotation(Bean.class);
		String beanName = bean.value();
		Class<?> beanMethodReturnType = method.getReturnType();
		if (beanName.isEmpty()) {
			// 返回值首字母小写作为beanName
			beanName = StringUtils.initialLowerCase(beanMethodReturnType.getSimpleName());
		}
		
		// 若此beanName所代表的Bean已注册，则忽略之后的同beanName的Bean注册
		if (this.registry.containsBeanDefinition(beanName)) {
			logger.info("重复的在BeanFactory之中注册同一名称的Bean，将忽略第一次注册成功之后的其他Bean。by name："+beanName+" method："+method.getName());
			return ;
		}
		
		RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(beanMethodReturnType);
		rootBeanDefinition.setBeanName(beanName);
		rootBeanDefinition.setFactoryBeanName(factoryBeanName);
		rootBeanDefinition.setFactoryMethodReturnType(beanMethodReturnType);
		// 保存已解析的工厂方法对象
		rootBeanDefinition.setResolvedConstructorOrFactoryMethod(method);
		rootBeanDefinition.setFactoryMethodUnique(true);
		rootBeanDefinition.setFactoryMethodName(method.getName());
		rootBeanDefinition.setTargetType(beanMethodReturnType);
		
		rootBeanDefinition.setAutowireCandidate(bean.autowireCandidate());
		// 保存此@Bean注解标注方法的返回值的注解信息
		rootBeanDefinition.setAnnotationMetadata(new AnnotationMetadataHolder(beanMethodReturnType));
		String initMethod = bean.initMethod();
		if (Assert.hasText(initMethod)) rootBeanDefinition.registerInitMethod(initMethod);
		
		String destroyMethod = bean.destroyMethod();
		if (Assert.hasText(destroyMethod)) rootBeanDefinition.registerDestroyMethod(destroyMethod);
		
		this.registry.registerBeanDefinition(beanName, rootBeanDefinition);
//		DebugUtils.log(logger, "BeanDefinition注册，由工厂方法实例化。by name："+beanName+" factoryMethod："+beanMethod.getName()+" factoryBean："+factoryBeanName);
	}
}

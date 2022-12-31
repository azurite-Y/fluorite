package org.zy.fluorite.context.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.BeanNameGenerator;
import org.zy.fluorite.context.annotation.interfaces.ImportBeanDefinitionRegistrar;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description {@link EnableConfigurationProperties @EnableConfigurationProperties} 导入的 {@link ImportBeanDefinitionRegistrar}
 */
public class EnableConfigurationPropertiesRegistrar implements ImportBeanDefinitionRegistrar {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
		EnableConfigurationProperties enableConfigurationProperties = metadata.getAnnotationAttributesForClass().getAnnotation(EnableConfigurationProperties.class);
		Class<?>[] value = enableConfigurationProperties.value();
		
		for (Class<?> configurationPropertiesClz : value) {
			RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(configurationPropertiesClz);
			
			String beanName = importBeanNameGenerator.generateBeanName(rootBeanDefinition, registry);
			rootBeanDefinition.setBeanName(beanName);
			
			DebugUtils.log(logger, "注册 '" +configurationPropertiesClz + "' 类BeanDefinition，by beanName："+beanName+" 源头："+metadata.getSourceClass().getName());
			registry.registerBeanDefinition(beanName, rootBeanDefinition);
			
		}
	}
	
	@Override
	public void invokeAwareMethods(Environment environment, BeanDefinitionRegistry registry) {
		
	}

}

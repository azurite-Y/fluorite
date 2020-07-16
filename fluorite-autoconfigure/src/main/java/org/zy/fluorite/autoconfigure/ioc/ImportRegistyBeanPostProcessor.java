package org.zy.fluorite.autoconfigure.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.BeanNameGenerator;
import org.zy.fluorite.context.annotation.interfaces.ImportBeanDefinitionRegistrar;
import org.zy.fluorite.context.support.CommonAnnotationBeanPostProcessor;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @DateTime 2020年6月30日 下午4:42:08;
 * @author zy(azurite-Y);
 * @Description
 */
public class ImportRegistyBeanPostProcessor implements  ImportBeanDefinitionRegistrar{
	public final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
			BeanNameGenerator importBeanNameGenerator) {
		RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
		String beanName = importBeanNameGenerator.generateBeanName(rootBeanDefinition, registry);
		rootBeanDefinition.setBeanName(beanName);
		DebugUtils.log(logger, "注册CommonAnnotationBeanPostProcessor 类BeanDefinition，by beanName："+beanName+" 源头："+metadata.getSourceClass().getName());
		registry.registerBeanDefinition(beanName, rootBeanDefinition);
	}
	
	@Override
	public void invokeAwareMethods(Environment environment , BeanDefinitionRegistry registry) {
		
	}

}

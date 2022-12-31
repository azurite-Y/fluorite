package org.zy.fluorite.autoconfigure.web.server;

import org.zy.fluorite.autoconfigure.web.servlet.customizer.ErrorPageRegistrarBeanPostProcessor;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.context.annotation.interfaces.ImportBeanDefinitionRegistrar;
import org.zy.fluorite.context.support.DefaultListableBeanFactory;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月10日;
 * @author zy(azurite-Y);
 * @description
 * 注册 {@link WebServerFactoryCustomizerBeanPostProcessor} Bean。通过 {@link ImportBeanDefinitionRegistrar} 注册，以便提前注册。
 */
public class ServletWebServerBeanPostProcessorsRegistrar implements ImportBeanDefinitionRegistrar {
	private ConfigurableListableBeanFactory beanFactory;

	@Override
	public void invokeAwareMethods(Environment environment, BeanDefinitionRegistry registry) {}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		if (registry instanceof DefaultListableBeanFactory) {
			this.beanFactory = (ConfigurableListableBeanFactory)registry;
		}
		registerSyntheticBeanIfMissing(registry, "webServerFactoryCustomizerBeanPostProcessor", WebServerFactoryCustomizerBeanPostProcessor.class);
		registerSyntheticBeanIfMissing(registry, "errorPageRegistrarBeanPostProcessor", ErrorPageRegistrarBeanPostProcessor.class);
	}

	private void registerSyntheticBeanIfMissing(BeanDefinitionRegistry registry, String name, Class<?> beanClass) {
		if ( !Assert.notNull(this.beanFactory.getBeanNamesForType(beanClass, true, false)) ) {
			RootBeanDefinition beanDefinition = new RootBeanDefinition(beanClass);
			registry.registerBeanDefinition(name, beanDefinition);
		}
	}
}

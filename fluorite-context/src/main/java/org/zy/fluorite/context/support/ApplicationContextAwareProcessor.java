package org.zy.fluorite.context.support;

import org.zy.fluorite.beans.factory.aware.EmbeddedValueResolverAware;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanPostProcessor;
import org.zy.fluorite.beans.factory.support.EmbeddedValueResolver;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;
import org.zy.fluorite.context.interfaces.aware.ApplicationContextAware;
import org.zy.fluorite.context.interfaces.aware.ApplicationEventPublisherAware;
import org.zy.fluorite.context.interfaces.aware.MessageSourceAware;
import org.zy.fluorite.core.environment.AbstractEnvironment;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.EnvironmentAware;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.interfaces.PriorityOrdered;
import org.zy.fluorite.core.interfaces.StringValueResolver;

/**
 * @DateTime 2020年6月17日 下午4:34:27;
 * @author zy(azurite-Y);
 * @Description BeanPostProcessor实现集合的首个后处理器，为实现了相关Aware子类的bean提供方法调用服务
 */ 
public class ApplicationContextAwareProcessor implements BeanPostProcessor,PriorityOrdered  {
	private final ConfigurableApplicationContext applicationContext;
	private final StringValueResolver embeddedValueResolver;
	
	public ApplicationContextAwareProcessor(ConfigurableApplicationContext applicationContext,	StringValueResolver embeddedValueResolver) {
		super();
		this.applicationContext = applicationContext;
		this.embeddedValueResolver = embeddedValueResolver;
	}

	public ApplicationContextAwareProcessor(ConfigurableApplicationContext context) {
		this.applicationContext = context;
		this.embeddedValueResolver = new EmbeddedValueResolver(
				(AbstractEnvironment) applicationContext.getEnvironment(),applicationContext.getBeanFactory());
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, BeanDefinition definition) throws BeansException {
		if (!(bean instanceof EnvironmentAware || bean instanceof EmbeddedValueResolverAware
				|| bean instanceof ApplicationEventPublisherAware || bean instanceof MessageSourceAware
				|| bean instanceof ApplicationContextAware)){
			return bean;
		}
		invokeAwareInterfaces(bean);
		return bean;
	}

	private void invokeAwareInterfaces(Object bean) {
		if (bean instanceof EnvironmentAware) {
			((EnvironmentAware) bean).setEnvironment(this.applicationContext.getEnvironment());
		}
		if (bean instanceof EmbeddedValueResolverAware) {
			((EmbeddedValueResolverAware) bean).setEmbeddedValueResolver(this.embeddedValueResolver);
		}
		if (bean instanceof ApplicationEventPublisherAware) {
			((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(this.applicationContext);
		}
		if (bean instanceof MessageSourceAware) {
			((MessageSourceAware) bean).setMessageSource(this.applicationContext);
		}
		if (bean instanceof ApplicationContextAware) {
			((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
		}
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
	
}

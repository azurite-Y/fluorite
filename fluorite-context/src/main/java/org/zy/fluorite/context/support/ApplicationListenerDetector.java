package org.zy.fluorite.context.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.interfaces.processor.DestructionAwareBeanPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.MergedBeanDefinitionPostProcessor;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.context.event.interfaces.ApplicationEventMulticaster;
import org.zy.fluorite.context.event.interfaces.ApplicationListener;
import org.zy.fluorite.core.annotation.Order;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @DateTime 2020年6月17日 下午4:53:56;
 * @author zy(azurite-Y);
 * @Description 收集ApplicationListener类型的单例Bean并注册到上下文中，且保证收集到的单例Bean的能正常的被销毁
 */
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
public class ApplicationListenerDetector implements DestructionAwareBeanPostProcessor, MergedBeanDefinitionPostProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationListenerDetector.class);

	private final transient AbstractApplicationContext applicationContext;

	/** 存储ApplicationListener类型的bean是否是单例bean的映射关系 */
	private final transient Map<String, Boolean> singletonNames = new ConcurrentHashMap<>(256);

	public ApplicationListenerDetector(AbstractApplicationContext abstractApplicationContext) {
		this.applicationContext = abstractApplicationContext;
	}

	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		if (ApplicationListener.class.isAssignableFrom(beanType)) {
			this.singletonNames.put(beanName, beanDefinition.isSingleton());
		}
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, BeanDefinition definition) throws BeansException {
		if (bean instanceof ApplicationListener) {
			String beanName = definition.getBeanName();
			Boolean flag = this.singletonNames.get(beanName);
			if (Boolean.TRUE.equals(flag)) {
				this.applicationContext.addApplicationListener((ApplicationListener<?>) bean);
			} else if (Boolean.FALSE.equals(flag)) {
				if (logger.isWarnEnabled() && !this.applicationContext.containsBean(beanName)) {
					logger.warn("非单例Bean无法使用事件多播，by：bean：" + beanName);
				}
				this.singletonNames.remove(beanName);
			}
		}
		return bean;
	}

	@Override
	public void postProcessBeforeDestruction(Object bean, BeanDefinition definition) throws BeansException {
		if (bean instanceof ApplicationListener) {
			try {
				ApplicationEventMulticaster multicaster = this.applicationContext.getApplicationEventMulticaster();
				multicaster.removeApplicationListener((ApplicationListener<?>) bean);
				multicaster.removeApplicationListenerBean(definition.getBeanName());
			} catch (IllegalStateException ex) {
				// ApplicationEventMulticaster尚未初始化-无需删除侦听器
			}
		}
	}

	@Override
	public boolean requiresDestruction(Object bean,BeanDefinition definition) {
		return (bean instanceof ApplicationListener);
	}
}

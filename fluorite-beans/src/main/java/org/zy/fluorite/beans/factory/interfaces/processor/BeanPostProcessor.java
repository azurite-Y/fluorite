package org.zy.fluorite.beans.factory.interfaces.processor;

import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy;
 * @DateTime: 2020年6月4日 下午3:33:40;
 * @Description IOC功能顶层接口之一，Spring在此定义了bean初始化操作前后的回调方法
 */
public interface BeanPostProcessor {
	/**
	 * 在进行bean的初始化操作之前应用的后处理器
	 * 
	 * @param bean
	 * @param beanDefinition
	 * @return
	 * @throws BeansException
	 */
	default Object postProcessBeforeInitialization(Object bean, BeanDefinition beanDefinition) throws BeansException {
		return bean;
	} 

	/**
	 * 在进行bean的初始化操作之后应用的后处理器
	 * 
	 * @param bean - 实例Bean
	 * @param mbd - 注册的BeanDefinition
	 * @return 要使用的bean实例，可以是原始实例，也可以是已包装的实例；如果为null，则不会调用后续的beanPostProcessor
	 * @throws BeansException
	 */
	default Object postProcessAfterInitialization(Object bean, BeanDefinition mbd) throws BeansException {
		return bean;
	}
	
}

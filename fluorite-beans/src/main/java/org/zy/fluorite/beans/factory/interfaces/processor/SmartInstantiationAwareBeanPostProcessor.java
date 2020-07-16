package org.zy.fluorite.beans.factory.interfaces.processor;

import java.lang.reflect.Constructor;

import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月10日 下午6:11:38;
 * @Description InstantiationAwareBeanPostProcessor接口的扩展，添加用于预测已处理bean最终类型的回调
 */
public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor {
	/**
	 * 预测最终从该处理器的postProcessBeforeInstantiation回调返回的bean的类型。
	 * 默认实现返回空值。
	 */
	default Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	/**
	 * 确定要用于给定bean的候选构造函数。默认实现返回空值。
	 */
	default Constructor<?>[] determineCandidateConstructors(BeanDefinition beanDefinition) throws BeansException {
		return null;
	}

	/**
	 * 获取对指定bean的早期访问的引用，通常用于解析循环引用。
	 * 这个回调使后处理器有机会提前公开未经过postProcessBeforeInitialization/postprocessAfterInitialization方法的实例。
	 * @param bean - Bean实例
	 * @param definition
	 * @return
	 * @throws BeansException
	 */
	default Object getEarlyBeanReference(Object bean, BeanDefinition definition) throws BeansException {
		return bean;
	}

}

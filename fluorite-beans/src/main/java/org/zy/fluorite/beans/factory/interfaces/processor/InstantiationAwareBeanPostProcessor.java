package org.zy.fluorite.beans.factory.interfaces.processor;

import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.beans.interfaces.PropertyValues;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy;
 * @DateTime: 2020年6月4日 下午3:39:18;
 * @Description bean对象实例化前后的回调方法定义接口
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {
	/**
	 * bean对象实例化之前的回调方法
	 * @param beanName
	 * @param bd
	 * @return 要公开的bean对象而不是目标bean的默认实例，或者null以继续默认实例化
	 * @throws BeansException
	 */
	default Object postProcessBeforeInstantiation(String beanName, BeanDefinition bd) throws BeansException {
		return null;
	}
	
	/**
	 * bean对象实例化之后的回调方法，可返回false停止继续调用下一个postProcessAfterInstantiation方法
	 * @param bean
	 * @param bd
	 * @return
	 * @throws BeansException
	 */
	default boolean postProcessAfterInstantiation(Object bean, BeanDefinition bd) throws BeansException {
		return true;
	}
	
	/**
	 * 在工厂将给定的属性值应用于给定bean之前对其进行后处理，而不需要任何属性描述符。直接设置属性值或调用方法
	 * @param pvs
	 * @param bean
	 * @param beanName
	 * @return
	 * @throws BeansException
	 */
	default PropertyValues postProcessProperties(PropertyValues pvs, Object bean, BeanDefinition beanDefinition)
			throws BeansException {
		return pvs;
	}
}

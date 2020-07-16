package org.zy.fluorite.beans.factory.interfaces.processor;

import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午4:08:22;
 * @Description 处理BeanDefinition的回调方法定义接口
 */
public interface MergedBeanDefinitionPostProcessor extends BeanPostProcessor {

	/**
	 * 处理BeanDefinition的回调方法
	 * @param beanDefinition
	 * @param beanType
	 * @param beanName
	 */
	void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);
	
	/**
	 * 将指定名称的bean定义重置，并且应清除此后处理器中受影响bean的任何元数据。
	 * @param beanName
	 */
	default void resetBeanDefinition(String beanName) {
	}

}

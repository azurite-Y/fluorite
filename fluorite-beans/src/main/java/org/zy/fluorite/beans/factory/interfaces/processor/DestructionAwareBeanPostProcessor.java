package org.zy.fluorite.beans.factory.interfaces.processor;

import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午3:58:59;
 * @Description  bean对象销毁之前的回调方法定义接口
 */
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {
	/**
	 * 在销毁bean之前应用的后处理器方法
	 * @param bean
	 * @param definition
	 * @throws BeansException
	 */
	void postProcessBeforeDestruction(Object bean, BeanDefinition definition) throws BeansException;

	/**
	 * 确定给定的bean实例是否需要此后处理器销毁。
	 * @param bean
	 * @return
	 */
	default boolean requiresDestruction(Object bean, BeanDefinition definition) {
		return true;
	}
}

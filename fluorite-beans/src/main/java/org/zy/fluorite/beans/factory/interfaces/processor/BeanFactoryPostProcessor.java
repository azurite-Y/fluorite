package org.zy.fluorite.beans.factory.interfaces.processor;

import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @DateTime 2020年6月17日 下午1:55:13;
 * @author zy(azurite-Y);
 * @Description 工厂钩子，允许自定义修改应用程序上下文的bean定义，调整上下文的底层bean工厂的bean属性值.
 * 不直接与Bean实例交互
 */
@FunctionalInterface
public interface BeanFactoryPostProcessor {
	
	/**
	 * 在标准初始化之后修改应用程序上下文的内部bean工厂。将加载所有bean定义，但尚未实例化任何beans
	 * @param beanFactory
	 * @throws BeansException
	 */
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;
	
}

package org.zy.fluorite.beans.factory.interfaces.processor;

import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @DateTime 2020年6月19日 下午1:34:33;
 * @author zy(azurite-Y);
 * @Description 扩展到标准BeanFactoryPostProcessor
 *              SPI，允许在常规BeanFactoryPostProcessor检测开始之前注册更多的bean定义。具体来说，BeanDefinitionRegistryPostProcessor可以注册进一步的bean定义，进而定义BeanFactoryPostProcessor实例
 */
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {
	/**
	 *在标准初始化之后修改应用程序上下文的内部bean定义注册表。所有常规bean定义都将被加载，但是还没有bean被实例化。
	 *这允许在下一个后处理阶段开始之前添加进一步的bean定义。
	 */
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

}

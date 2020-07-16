package org.zy.fluorite.context.annotation.interfaces;

import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.core.environment.interfaces.Environment;

/**
 * @DateTime 2020年6月20日 下午1:17:30;
 * @author zy(azurite-Y);
 * @Description 条件上下文
 */
public interface ConditionContext {
	/**
	 * 如果条件匹配，将返回保存bean定义的BeanDefinitionRegistry
	 */
	BeanDefinitionRegistry getRegistry();

	/**
	 * 如果条件匹配，则返回将保存bean定义的ConfigurableListableBeanFactory；
	 * 如果bean工厂不可用（或无法向下转换为ConfigurableListableBeanFactory），则返回null。
	 */
	ConfigurableListableBeanFactory getBeanFactory();

	/**
	 * 返回当前应用程序正在运行的环境
	 */
	Environment getEnvironment();

	/**
	 * 返回应用于加载其他类的类加载器（仅当系统类加载器不可访问时为空）。
	 */
	ClassLoader getClassLoader();
}

package org.zy.fluorite.aop.interfaces.function;

import org.zy.fluorite.aop.interfaces.TargetSource;

/**
 * @DateTime 2020年7月5日 上午8:38:17;
 * @author zy(azurite-Y);
 * @Description 实现可以为特定bean创建特殊的目标源。实现可以为特定bean创建特殊的目标源
 */
@FunctionalInterface
public interface TargetSourceCreator {
	/**
	 * 为给定的bean创建一个特殊的TargetSource（如果有的话）
	 * @param beanClass - 要为其创建TargetSource的bean的类
	 * @param beanName  
	 */
	TargetSource getTargetSource(Class<?> beanClass, String beanName);
}

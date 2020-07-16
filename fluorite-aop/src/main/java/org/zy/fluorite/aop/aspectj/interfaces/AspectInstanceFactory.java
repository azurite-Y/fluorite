package org.zy.fluorite.aop.aspectj.interfaces;

import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @DateTime 2020年7月5日 下午3:39:14;
 * @author zy(azurite-Y);
 * @Description
 */
public interface AspectInstanceFactory extends Ordered {
	/** 创建此工厂切面的实例 */
	Object getAspectInstance();

	/** 公开此工厂使用的切面类加载器 */
	ClassLoader getAspectClassLoader();
	
	/** 获得切面工厂包装的切面Bean的beanName */
	String getName();
	
	/** 获得切面工厂包装的切面Bean的类型 */
	Class<?> getType();
}

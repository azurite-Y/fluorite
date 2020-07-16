package org.zy.fluorite.aop.interfaces;

import org.zy.fluorite.core.utils.ClassUtils;

/**
 * @DateTime 2020年7月4日 下午4:48:54;
 * @author zy(azurite-Y);
 * @Description
 */
public interface AopProxy {
	
	/** 使用线程上下文类加载器创建新的代理对象 */
	default Object getProxy() {
		return getProxy(ClassUtils.getDefaultClassLoader());
	}

	/** 使用给定的类加载器创建新的代理对象 */
	Object getProxy(ClassLoader classLoader);
}

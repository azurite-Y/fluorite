package org.zy.fluorite.core.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午3:25:19;
 * @Description 参数名发现程序
 */
public interface ParameterNameDiscoverer {
	/**
	 * 从指定的方法对象中获得参数名集合
	 * @param method
	 * @return
	 */
	String[] getParameterNames(Method method);

	/**
	 * 从指定的构造器对象中获得参数名集合
	 * @param method
	 * @return
	 */
	String[] getParameterNames(Constructor<?> ctor);
}

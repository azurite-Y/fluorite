package org.zy.fluorite.core.interfaces;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午3:59:04;
 * @Description 方法过滤
 */
public interface MethodFilter {
	/**
	 * 过滤方法
	 * @param methods
	 * @return
	 */
	List<Method> matcher(List<Method> methods);
	
	/**
	 * 过滤方法
	 * @param methods
	 * @return
	 */
	List<Method> matcher(Method[]  methods);
}

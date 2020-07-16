package org.zy.fluorite.aop.interfaces;

import java.lang.reflect.Method;

/**
 * @DateTime 2020年7月5日 上午8:36:23;
 * @author zy(azurite-Y);
 * @Description
 */
public interface MethodInvocation extends Invocation {
	/**
	 * 获取被调用的方法
	 * @return 正在调用的方法
	 */
	Method getMethod();
}

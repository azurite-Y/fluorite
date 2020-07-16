package org.zy.fluorite.aop.aspectj.interfaces;

import java.lang.reflect.Method;

/**
 * @DateTime 2020年7月6日 下午7:28:46;
 * @author zy(azurite-Y);
 * @Description 方法返回通知只在普通方法返回时调用，而不是在抛出异常时调用。
 * 这样的Advice可以看到返回值，但不能更改它。
 */
public interface AfterReturningAdvice extends AfterAdvice{
	
	/**
	 * 成给定方法功返回后的回调。
	 * @param returnValue - 方法返回值
	 * @param method - 调用的方法
	 * @param args - 调用方法的参数集
	 * @param target - 方法调用的目标。可能为空
	 * @throws Throwable - 如果希望终止方法调用而抛出的异常为方法签名异常则返回给调用方，否则将包装为一个运行时异常
	 */
	void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}

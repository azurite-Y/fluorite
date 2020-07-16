package org.zy.fluorite.aop.aspectj.interfaces;

import java.lang.reflect.Method;

/**
 * @DateTime 2020年7月6日 下午7:13:28;
 * @author zy(azurite-Y);
 * @Description 在调用方法之前调用的通知，若需要阻止方法的调用需抛出一个异常
 */
public interface MethodBeforeAdvice  extends BeforeAdvice {
	/**
	 * 调用给定方法之前的回调
	 * @param method - 正在调用的方法
	 * @param args - 调用方法的参数
	 * @param target - 方法调用的目标。可能为空
	 * @throws Throwable - 如果希望终止方法调用而抛出的异常为方法签名异常则返回给调用方，否则将包装为一个运行时异常
	 */
	void before(Method method, Object[] args, Object target) throws Throwable;
}

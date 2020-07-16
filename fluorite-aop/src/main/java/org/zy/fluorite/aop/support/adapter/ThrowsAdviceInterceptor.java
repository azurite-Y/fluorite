package org.zy.fluorite.aop.support.adapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.aop.aspectj.interfaces.AfterAdvice;
import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.ThrowsAdvice;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @DateTime 2020年7月6日 下午11:54:33;
 * @author zy(azurite-Y);
 * @Description ThrowsAdvice的拦截器
 * @see ThrowsAdvice (异常通知方法定义示例)
 */
public class ThrowsAdviceInterceptor implements MethodInterceptor, AfterAdvice {

	private static final String AFTER_THROWING = "afterThrowing";

	private static final Logger logger = LoggerFactory.getLogger(ThrowsAdviceInterceptor.class);

	private final Object throwsAdvice;

	/** 异常类 : 异常通知方法 */
	private final Map<Class<?>, Method> exceptionHandlerMap = new HashMap<>();

	/** 为给定的ThrowsAdvice创建一个新的throwAdvice拦截器 */
	public ThrowsAdviceInterceptor(Object throwsAdvice) {
		Assert.notNull(throwsAdvice, "Advice不能为null");
		this.throwsAdvice = throwsAdvice;

		Method[] methods = throwsAdvice.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().equals(AFTER_THROWING) && 
					(method.getParameterCount() == 1 || method.getParameterCount() == 4)) {
				Class<?> throwableParam = method.getParameterTypes()[method.getParameterCount() - 1];
				if (Throwable.class.isAssignableFrom(throwableParam)) {
					this.exceptionHandlerMap.put(throwableParam, method);
					DebugUtils.logFromAop(logger, "找到的异常通知方法：" + method);
				}
			}
		}
		Assert.notNull(this.exceptionHandlerMap, "类中必须至少找到一个处理程序方法，by class：" + throwsAdvice.getClass());
	}

	public int getHandlerMethodCount() {
		return this.exceptionHandlerMap.size();
	}

	/** 驳货连接点调用的异常，然后将此异常交给异常通知来处理  */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			return invocation.proceed();
		} catch (Throwable ex) {
			Method handlerMethod = getExceptionHandler(ex);
			if (handlerMethod != null) {
				invokeHandlerMethod(invocation, ex, handlerMethod);
			}
			throw ex;
		}
	}

	/**
	 * 确定给定异常的异常通知方法
	 * @param exception - 连接点抛出的异常
	 * @return 返回异常处理集合中与之对应的异常通知，若未找到则返回null
	 */
	private Method getExceptionHandler(Throwable exception) {
		Class<?> exceptionClass = exception.getClass();
		Method handler = this.exceptionHandlerMap.get(exceptionClass);
		while (handler == null && exceptionClass != Throwable.class) {
			exceptionClass = exceptionClass.getSuperclass();
			handler = this.exceptionHandlerMap.get(exceptionClass);
		}
		return handler;
	}

	/**
	 * 调用异常通知方法
	 * @param invocation
	 * @param ex
	 * @param method
	 * @throws Throwable
	 */
	private void invokeHandlerMethod(MethodInvocation invocation, Throwable ex, Method method) throws Throwable {
		Object[] handlerArgs;
		// 根据异常通知方法的参数数目，获取连接点的相关信息构建参数集
		if (method.getParameterCount() == 1) {
			handlerArgs = new Object[] { ex };
		} else {
			handlerArgs = new Object[] { invocation.getMethod(), invocation.getArguments(), invocation.getThis(), ex };
		}
		try {
			method.invoke(this.throwsAdvice, handlerArgs);
		} catch (InvocationTargetException targetEx) {
			throw targetEx.getTargetException();
		}
	}

}

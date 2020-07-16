package org.zy.fluorite.aop.support.adapter;

import java.io.Serializable;

import org.zy.fluorite.aop.aspectj.interfaces.AfterAdvice;
import org.zy.fluorite.aop.aspectj.interfaces.AfterReturningAdvice;
import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月6日 下午11:46:37;
 * @author zy(azurite-Y);
 * @Description AfterReturningAdvice的拦截器
 */
@SuppressWarnings("serial")
public class AfterReturningAdviceInterceptor implements MethodInterceptor, AfterAdvice, Serializable {
	private final AfterReturningAdvice advice;

	public AfterReturningAdviceInterceptor(AfterReturningAdvice advice) {
		Assert.notNull(advice, "Advice不能为null");
		this.advice = advice;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object retVal = invocation.proceed();
		this.advice.afterReturning(retVal, invocation.getMethod(), invocation.getArguments(), invocation.getThis());
		return retVal;
	}
}

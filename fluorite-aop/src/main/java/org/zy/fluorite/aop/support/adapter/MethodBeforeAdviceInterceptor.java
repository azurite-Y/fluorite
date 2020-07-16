package org.zy.fluorite.aop.support.adapter;

import java.io.Serializable;

import org.zy.fluorite.aop.aspectj.interfaces.BeforeAdvice;
import org.zy.fluorite.aop.aspectj.interfaces.MethodBeforeAdvice;
import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月6日 下午11:41:09;
 * @author zy(azurite-Y);
 * @Description 负责MethodBeforeAdvice的调度
 */
@SuppressWarnings("serial")
public class MethodBeforeAdviceInterceptor  implements MethodInterceptor, BeforeAdvice, Serializable {
	private final MethodBeforeAdvice advice;
	
	public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
		Assert.notNull(advice, "Advice不能为null");
		this.advice = advice;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		this.advice.before(invocation.getMethod(), invocation.getArguments(), invocation.getThis());
		return invocation.proceed();
	}
}

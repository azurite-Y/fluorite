package org.zy.fluorite.aop.aspectj.advice;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.zy.fluorite.aop.aspectj.interfaces.AspectInstanceFactory;
import org.zy.fluorite.aop.aspectj.interfaces.JoinPointMatch;
import org.zy.fluorite.aop.aspectj.interfaces.ProceedingJoinPoint;
import org.zy.fluorite.aop.aspectj.support.AspectJPluralisticPointcut;
import org.zy.fluorite.aop.aspectj.support.MethodInvocationProceedingJoinPoint;
import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.ProxyMethodInvocation;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月6日 下午6:46:51;
 * @author zy(azurite-Y);
 * @Description 环绕通知包装类
 */
@SuppressWarnings("serial")
public class AspectJAroundAdvice  extends AbstractAspectJAdvice implements MethodInterceptor, Serializable  {
	public AspectJAroundAdvice(Method aspectJAroundAdviceMethod, AspectJPluralisticPointcut pointcut, AspectInstanceFactory aif) {
		super(aspectJAroundAdviceMethod, pointcut, aif);
	}

	@Override
	public boolean isBeforeAdvice() {
		return false;
	}

	@Override
	public boolean isAfterAdvice() {
		return false;
	}

	@Override
	protected boolean supportsProceedingJoinPoint() {
		return true;
	}
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Assert.isTrue(invocation instanceof ProxyMethodInvocation , "提供的MethodInvocation对象未实现ProxyMethodInvocation接口，by："+invocation);
		ProxyMethodInvocation pmi = (ProxyMethodInvocation) invocation;
		ProceedingJoinPoint pjp = lazyGetProceedingJoinPoint(pmi);
		JoinPointMatch jpm = getJoinPointMatch(pmi);
		return invokeAdviceMethod(pjp, jpm, null, null);
	}

	private ProceedingJoinPoint lazyGetProceedingJoinPoint(ProxyMethodInvocation pmi) {
		return new MethodInvocationProceedingJoinPoint(pmi);
	}

}

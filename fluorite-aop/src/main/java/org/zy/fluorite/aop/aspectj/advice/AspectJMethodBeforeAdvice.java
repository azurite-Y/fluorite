package org.zy.fluorite.aop.aspectj.advice;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.zy.fluorite.aop.aspectj.interfaces.AspectInstanceFactory;
import org.zy.fluorite.aop.aspectj.interfaces.MethodBeforeAdvice;
import org.zy.fluorite.aop.aspectj.support.AspectJPluralisticPointcut;

/**
 * @DateTime 2020年7月6日 下午7:12:24;
 * @author zy(azurite-Y);
 * @Description 前置通知包装类
 */
@SuppressWarnings("serial")
public class AspectJMethodBeforeAdvice  extends AbstractAspectJAdvice implements MethodBeforeAdvice, Serializable {

	public AspectJMethodBeforeAdvice(Method aspectJAdviceMethod, AspectJPluralisticPointcut pointcut,AspectInstanceFactory aspectInstanceFactory) {
		super(aspectJAdviceMethod, pointcut, aspectInstanceFactory);
	}

	@Override
	public boolean isBeforeAdvice() {
		return true;
	}

	@Override
	public boolean isAfterAdvice() {
		return false;
	}

	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		invokeAdviceMethod(super.getJoinPointMatch(), null, null);
	}
}

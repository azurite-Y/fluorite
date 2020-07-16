package org.zy.fluorite.aop.aspectj.advice;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.zy.fluorite.aop.aspectj.interfaces.AfterAdvice;
import org.zy.fluorite.aop.aspectj.interfaces.AspectInstanceFactory;
import org.zy.fluorite.aop.aspectj.support.AspectJPluralisticPointcut;
import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;

/**
 * @DateTime 2020年7月6日 下午7:26:30;
 * @author zy(azurite-Y);
 * @Description 后置通知包装类
 */
@SuppressWarnings("serial")
public class AspectJAfterAdvice extends AbstractAspectJAdvice implements MethodInterceptor, AfterAdvice, Serializable {

	public AspectJAfterAdvice(Method aspectJAdviceMethod, AspectJPluralisticPointcut pointcut ,	AspectInstanceFactory aspectInstanceFactory) {
		super(aspectJAdviceMethod, pointcut, aspectInstanceFactory);
	}

	@Override
	public boolean isBeforeAdvice() {
		return false;
	}

	@Override
	public boolean isAfterAdvice() {
		return true;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			return invocation.proceed();
		} finally {
			invokeAdviceMethod(getJoinPointMatch(), null, null);
		}
	}

}

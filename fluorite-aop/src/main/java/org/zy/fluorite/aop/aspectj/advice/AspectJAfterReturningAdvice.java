package org.zy.fluorite.aop.aspectj.advice;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.zy.fluorite.aop.aspectj.interfaces.AfterAdvice;
import org.zy.fluorite.aop.aspectj.interfaces.AfterReturningAdvice;
import org.zy.fluorite.aop.aspectj.interfaces.AspectInstanceFactory;
import org.zy.fluorite.aop.aspectj.support.AspectJPluralisticPointcut;

/**
 * @DateTime 2020年7月6日 下午7:35:02;
 * @author zy(azurite-Y);
 * @Description 方法成功返回后所调通知的包装类
 */
@SuppressWarnings("serial")
public class AspectJAfterReturningAdvice extends AbstractAspectJAdvice implements AfterReturningAdvice, AfterAdvice, Serializable {

	public AspectJAfterReturningAdvice(Method aspectJAdviceMethod, AspectJPluralisticPointcut pointcut ,	AspectInstanceFactory aspectInstanceFactory) {
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
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		invokeAdviceMethod(getJoinPointMatch(), returnValue, null);
	}
}

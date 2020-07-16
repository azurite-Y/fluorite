package org.zy.fluorite.aop.aspectj.advice;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.zy.fluorite.aop.aspectj.interfaces.AfterAdvice;
import org.zy.fluorite.aop.aspectj.interfaces.AspectInstanceFactory;
import org.zy.fluorite.aop.aspectj.support.AspectJPluralisticPointcut;
import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;

/**
 * @DateTime 2020年7月6日 下午7:38:34;
 * @author zy(azurite-Y);
 * @Description 异常通知包装类
 */
@SuppressWarnings("serial")
public class AspectJAfterThrowingAdvice extends AbstractAspectJAdvice implements MethodInterceptor, AfterAdvice, Serializable {
	
	public AspectJAfterThrowingAdvice(Method aspectJBeforeAdviceMethod, AspectJPluralisticPointcut pointcut,	AspectInstanceFactory aif) {
		super(aspectJBeforeAdviceMethod, pointcut, aif);
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
	public Object invoke(MethodInvocation mi) throws Throwable {
		try {
			return mi.proceed();
		} catch (Throwable ex) {
			/**
			 * 在默认的切点匹配逻辑中，唯独没有对方法签名抛出的异常类型做验证，
			 * 所以特在此进行抛出异常验证，若匹配则调用此异常通知方法，未匹配则不调用。
			 */
			if (shouldInvokeOnThrowing(ex)) {
				invokeAdviceMethod(super.getJoinPointMatch(), null, ex);
			}
			throw ex;
		}
	}

	/** 当抛出的异常是给定throwingtype的子类型时，才会调用指定抛出子句的advice */
	private boolean shouldInvokeOnThrowing(Throwable ex) {
		return super.getThrowingType().isAssignableFrom(ex.getClass());
	}
}

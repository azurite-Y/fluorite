package org.zy.fluorite.aop.support.adapter;

import java.io.Serializable;

import org.zy.fluorite.aop.aspectj.interfaces.AfterReturningAdvice;
import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.aop.interfaces.AdvisorAdapter;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;

/**
 * @DateTime 2020年7月6日 下午11:43:23;
 * @author zy(azurite-Y);
 * @Description AfterReturningAdvice的适配器
 */
@SuppressWarnings("serial")
public class AfterReturningAdviceAdapter implements AdvisorAdapter, Serializable {
	@Override
	public boolean supportsAdvice(Advice advice) {
		return (advice instanceof AfterReturningAdvice);
	}

	@Override
	public MethodInterceptor getInterceptor(Advisor advisor) {
		AfterReturningAdvice advice = (AfterReturningAdvice) advisor.getAdvice();
		return new AfterReturningAdviceInterceptor(advice);
	}
}

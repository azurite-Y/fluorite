package org.zy.fluorite.aop.support.adapter;

import java.io.Serializable;

import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.aop.interfaces.AdvisorAdapter;
import org.zy.fluorite.aop.interfaces.ThrowsAdvice;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;

/**
 * @DateTime 2020年7月6日 下午11:50:00;
 * @author zy(azurite-Y);
 * @Description MethodBeforeAdvice的拦截器
 */
@SuppressWarnings("serial")
public class ThrowsAdviceAdapter implements AdvisorAdapter, Serializable  {
	@Override
	public boolean supportsAdvice(Advice advice) {
		return (advice instanceof ThrowsAdvice);
	}

	@Override
	public MethodInterceptor getInterceptor(Advisor advisor) {
		return new ThrowsAdviceInterceptor(advisor.getAdvice());
	}

}

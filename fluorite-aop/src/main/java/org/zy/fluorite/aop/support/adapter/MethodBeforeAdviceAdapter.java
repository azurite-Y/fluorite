package org.zy.fluorite.aop.support.adapter;

import java.io.Serializable;

import org.zy.fluorite.aop.aspectj.interfaces.MethodBeforeAdvice;
import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.aop.interfaces.AdvisorAdapter;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;


/**
 * @DateTime 2020年7月6日 下午11:37:55;
 * @author zy(azurite-Y);
 * @Description MethodBeforeAdvice的适配器
 */
@SuppressWarnings("serial")
public class MethodBeforeAdviceAdapter implements AdvisorAdapter, Serializable  {

	@Override
	public boolean supportsAdvice(Advice advice) {
		return (advice instanceof MethodBeforeAdvice);
	}

	@Override
	public MethodInterceptor getInterceptor(Advisor advisor) {
		MethodBeforeAdvice advice = (MethodBeforeAdvice) advisor.getAdvice();
		return new MethodBeforeAdviceInterceptor(advice);
	}

}

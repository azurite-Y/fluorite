package org.zy.fluorite.aop.interfaces;

import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;

/**
 * @DateTime 2020年7月4日 下午6:55:47;
 * @author zy(azurite-Y);
 * @Description
 */
public interface AdvisorAdapter {
	/**
	 * 此适配器是否理解此通知对象
	 * @param advice - Advice或是BeforeAdvice
	 */
	boolean supportsAdvice(Advice advice);

	/** 返回一个AOP方法拦截器，将给定advisor的行为公开给基于拦截的AOP框架 */
	MethodInterceptor getInterceptor(Advisor advisor);
}

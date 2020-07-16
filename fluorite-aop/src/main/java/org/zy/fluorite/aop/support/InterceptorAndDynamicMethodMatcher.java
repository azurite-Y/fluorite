package org.zy.fluorite.aop.support;

import org.zy.fluorite.aop.interfaces.MethodMatcher;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;

/**
 * @DateTime 2020年7月7日 下午1:51:11;
 * @author zy(azurite-Y);
 * @Description 将MethodInterceptor实例与MethodMatcher组合在一起，用作advisor链中的元素
 */
final class InterceptorAndDynamicMethodMatcher {
	final MethodInterceptor interceptor;

	final MethodMatcher methodMatcher;

	public InterceptorAndDynamicMethodMatcher(MethodInterceptor interceptor, MethodMatcher methodMatcher) {
		this.interceptor = interceptor;
		this.methodMatcher = methodMatcher;
	}
}

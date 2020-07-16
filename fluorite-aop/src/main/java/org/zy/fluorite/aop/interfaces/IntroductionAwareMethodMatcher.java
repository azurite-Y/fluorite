package org.zy.fluorite.aop.interfaces;

import java.lang.reflect.Method;

/**
 * @DateTime 2020年7月5日 下午3:42:11;
 * @author zy(azurite-Y);
 * @Description 一种特殊类型的MethodMatcher，在匹配方法时考虑 Introduction
 */
public interface IntroductionAwareMethodMatcher extends MethodMatcher {
	/**
	 * 执行静态检查给定方法是否匹配。可以调用它来代替。
	 * 可以调用此方法代替 {@linkplain MethodMatcher#matches(Method, Class) } 方法
	 * @param pointcut - 要检查的静态或动态切点
	 * @param targetClass - 当前Bean的Class对象
	 * @param hasIntroductions - 这个bean的advisor链是否包含任何IntroductionAdvisor
	 * @return
	 */
	boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions);
}

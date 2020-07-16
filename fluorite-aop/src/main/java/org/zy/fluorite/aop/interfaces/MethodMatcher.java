package org.zy.fluorite.aop.interfaces;

import java.lang.reflect.Method;

import org.zy.fluorite.aop.support.TrueMethodMatcher;

/**
 * @DateTime 2020年7月5日 下午2:49:09;
 * @author zy(azurite-Y);
 * @Description 检查目标方法是否有资格获得advice
 */
public interface MethodMatcher {
	/**
	 * 判断此方法是否是静态的
	 * <p>如果此方法返回false或 {@linkplain #isRuntime()} 方法返回false时，
	 * 就不会调用{@linkplain #matches(Method, Class, Object...)}</p>
	 * @param method - 候选方法
	 * @param targetClass
	 * @return 
	 */
	boolean matches(Method method, Class<?> targetClass);

	boolean isRuntime();

	/**
	 * 只有在 {@linkplain #matches(Method, Class)} 方法和 {@linkplain #isRuntime() }方法返回true时才会调用此方法
	 * @param method - 候选方法
	 * @param targetClass
	 * @param args - 方法参数
	 * @return
	 */
	boolean matches(Method method, Class<?> targetClass, Object... args);

	/** 匹配所有方法的规范实例 */
	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;
}

package org.zy.fluorite.aop.interfaces;

import java.lang.reflect.Method;

/**
 * @DateTime 2020年7月5日 下午2:41:24;
 * @author zy(azurite-Y);
 * @Description
 */
public interface Pointcut {
	
	/**
	 * 判别给定类是否适配此切面，此方法为Advice的预筛选，筛选出适配此Class对象Advice
	 * @param clz
	 * @return true代表适配给定类，反之则不适配
	 */
	boolean matcher(Class<?> clz);
	
	/**
	 * 判别给定类中的给定方法是否适配当前切面。
	 * 此方法只有在 {@linkplain Pointcut#matcher(Class) }方法返回true时才会调用
	 * @param targetClass - 需要适配的类对象
	 * @param method - 需要适配的方法对象
	 * @return true则代表适配给定类的给定方法，false则不适配给定方法，但适配给定类的其他方法
	 */
	boolean matcher(Class<?> targetClass, Method method);
	
	/** 始终匹配的规范切入点实例 */
	Pointcut TRUE = TruePointcut.INSTANCE;
}

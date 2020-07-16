
package org.zy.fluorite.core.utils;

import java.util.Collection;
import java.util.function.Supplier;

/**
* @author zy
* @Date 2019-11-23 周六 下午 14:27:03
* @Description 断言工具类
* @version 
*/
public class Assert {
	/**
	 * 非空检查，若为null则抛出异常
	 * @param request
	 * @param string
	 */
	public static void notNull(Object obj, String message) {
		if(!notNull(obj)) throw new IllegalArgumentException(message);
	}
	
	/**
	 * 非空检查
	 * @param property
	 * @return true代表不为null
	 */
	public static boolean notNull(Object property) {
		return property != null;
	}

	/**
	 * 非空与空集检查，若为null或空集则抛出异常
	 * @param request
	 * @param string
	 */
	public static void notNull(Object[] obj, String message) {
		if(!notNull(obj)) throw new IllegalArgumentException(message);
	}
	
	/**
	 * 非空或空集检查，若不为null且不为空集则返回true
	 * @param obj - 数组类型参数
	 * @return
	 */
	public static boolean notNull(Object [] obj) {
		return ( obj == null || obj.length == 0 ) ? false : true;
	}

	/**
	 * 非空或空集检查
	 * @param obj
	 * @param message
	 */
	public static void notNull(Collection<?> obj, String message) {
		if(!notNull(obj))  throw new IllegalArgumentException(message);
	}

	/**
	 * 非空或空集检查，若不为null且不为空集则返回true
	 * @param obj - 集合类型参数
	 * @return
	 */
	public static boolean notNull(Collection<?> obj) {
		return ( obj == null || obj.isEmpty() ) ? false : true;
	}
	
	/**
	 * 空串检查，为空串则返回false
	 * @param str
	 * @return
	 */
	public static void hasText(String str, String message) {
		if (!hasText(str)) throw new IllegalArgumentException(message);
	}
	
	/**
	 * 空串检查，为空串则返回false
	 * @param str
	 * @return
	 */
	public static void hasText(String message,String... strs) {
		for (String string : strs) {
			if (!hasText(string)) throw new IllegalArgumentException(message);
		}
	}
	/**
	 * 空串检查，为空串则返回false
	 * @param str
	 * @param messageSupplier
	 * @return
	 */
	public static void hasText(String str, Supplier<String> messageSupplier) {
		hasText(str, messageSupplier.get());
	}

	/**
	 * 空串检查，为null或空串则返回false
	 * @param resourceName
	 * @param message
	 */
	public static boolean hasText(String resourceName) {
		return (resourceName == null || resourceName.isEmpty() )? false : true;
	}
	
	/**
	 * 条件检查，若条件不成立则抛出异常
	 * @param flag
	 * @param message
	 */
	public static void isTrue(boolean flag, String message) {
		if (!flag)	throw new IllegalArgumentException(message);
	}

	/**
	 * 条件判断
	 * @param flag
	 * @param message
	 */
	public static void isTrue(boolean flag, Supplier<String> messageSupplier){
		isTrue(flag, messageSupplier.get());
	}

//	public static void isTrueIfAbsent(boolean flag, Supplier<String> messageSupplier){
	
	
	/**
	 * 判断指定对象是否是指定类型的实例，不是则抛出异常
	 * @param requiredType
	 * @param obj
	 * @param string - 抛出异常的指定信息
	 */
	public static void isInstanceOf(Class<?> requiredType, Object obj, String msg) {
		isTrue(requiredType.isInstance(obj),msg );
	}
	
	/**
	 * 判断指定对象是否是指定类型的实例，不是则抛出异常
	 * @param requiredType
	 * @param obj
	 * @param string
	 */
	public static void isInstanceOf(Class<?> requiredType, Object obj) {
		notNull(requiredType , "'requiredType' 不能为null");
		notNull(obj , "'obj' 不能为null");
		isTrue(requiredType.isInstance(obj), () -> {
			StringBuilder builder = new StringBuilder();
			builder.append(obj.getClass().getName())
				.append("类不是 ")
				.append(requiredType.getName())
				.append("的实现或子类，无法进行类型转换。");
			return builder.toString();
		});
	}

	/**
	 * 判断claz是否是clazz的父类或它本身
	 * @param claz
	 * @param clazz
	 * @param message
	 */
	public static void isAssignable(Class<?> claz, Class<?> clazz, String message) {
		isTrue(claz.isAssignableFrom(clazz), message);
	}
	
	/**
	 * 判断claz是否是clazz的父类或它本身
	 * @param claz
	 * @param clazz
	 * @param message
	 */
	public static void isAssignable(Class<?> claz, Class<?> clazz) {
		isTrue(claz.isAssignableFrom(clazz), () -> {
			StringBuilder builder = new StringBuilder();
			builder.append(claz.getName())
				.append("不是 ")
				.append(clazz.getName())
				.append("的父类或它本身，无法进行类型转换。");
			return builder.toString();
		});
	}
}

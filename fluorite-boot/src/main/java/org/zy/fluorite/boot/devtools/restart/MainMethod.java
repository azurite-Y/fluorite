package org.zy.fluorite.boot.devtools.restart;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月27日;
 * @author zy(azurite-Y);
 * @description “main”方法位于正在运行的线程中
 */
class MainMethod {
	private final Method method;

	MainMethod() {
		this(Thread.currentThread());
	}

	MainMethod(Thread thread) {
		Assert.notNull(thread, "Thread must not be null");
		this.method = getMainMethod(thread);
	}

	private Method getMainMethod(Thread thread) {
		for (StackTraceElement element : thread.getStackTrace()) {
			if ("main".equals(element.getMethodName())) {
				Method method = getMainMethod(element);
				if (method != null) {
					return method;
				}
			}
		}
		throw new IllegalStateException("Unable to find main method");
	}

	private Method getMainMethod(StackTraceElement element) {
		try {
			Class<?> elementClass = Class.forName(element.getClassName());
			Method method = elementClass.getDeclaredMethod("main", String[].class);
			if (Modifier.isStatic(method.getModifiers())) {
				return method;
			}
		}
		catch (Exception ex) {
			// Ignore
		}
		return null;
	}

	/**
	 * 返回实际的主方法
	 * 
	 * @return 主要方法
	 */
	Method getMethod() {
		return this.method;
	}

	/**
	 * 返回声明类的名称
	 * 
	 * @return 声明类名
	 */
	String getDeclaringClassName() {
		return this.method.getDeclaringClass().getName();
	}
}

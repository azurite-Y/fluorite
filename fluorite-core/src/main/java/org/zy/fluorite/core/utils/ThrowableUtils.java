package org.zy.fluorite.core.utils;

import java.util.function.Consumer;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description 异常工具类
 */
public class ThrowableUtils {
	/**
	 * 如果给定异常是由特定异常类型引起的，则执行操作
	 * 
	 * @param <E> - 原因异常类型
	 * @param ex - 源异常
	 * @param causedBy - 所需的原因类型
	 * @param action - 要执行的动作
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Exception> void ifCausedBy(Exception ex, Class<E> causedBy, Consumer<E> action) {
		Throwable candidate = ex;
		while (candidate != null) {
			if (causedBy.isInstance(candidate)) {
				action.accept((E) candidate);
				return;
			}
			candidate = candidate.getCause();
		}
	}
}

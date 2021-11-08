package org.zy.fluorite.core.interfaces.function;

/**
 * @DateTime 2021年9月17日;
 * @author zy(azurite-Y);
 * @Description
 * @param <T> 返回值类型
 */
@FunctionalInterface
public interface InvocationCallback<T> {
	T proceedWithInvocation() throws Throwable;
}

package org.zy.fluorite.core.interfaces.function;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月10日 下午6:28:04;
 * @Description 
 * @param <T> - 返回值类型
 * @param <P> - 请求参数类型
 */
@FunctionalInterface
public interface ActiveFunction<T,P> {
	T active(P p) throws Throwable;
}

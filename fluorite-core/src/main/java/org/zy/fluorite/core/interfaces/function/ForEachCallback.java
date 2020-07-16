package org.zy.fluorite.core.interfaces.function;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 上午9:36:59;
 * @Description 应用于forEach方法,T-参数类型
 */
@FunctionalInterface
public interface ForEachCallback<T> {
	
	void action(T t);
}

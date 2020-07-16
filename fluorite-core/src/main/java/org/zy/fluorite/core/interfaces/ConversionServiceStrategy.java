package org.zy.fluorite.core.interfaces;

/**
 * @DateTime 2020年7月2日 上午10:53:18;
 * @author zy(azurite-Y);
 * @Description 类型转换服务调用策略
 */
public interface ConversionServiceStrategy {

	/**
	 * 将给定的源转换为目标类型。
	 * @param <T>
	 * @param obj
	 * @param clz
	 * @return
	 */
	<T> T convert(Object obj, Class<T> clz);

}

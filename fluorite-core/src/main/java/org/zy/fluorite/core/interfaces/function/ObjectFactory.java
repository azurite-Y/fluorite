package org.zy.fluorite.core.interfaces.function;

import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午4:48:02;
 * @Description 间接调用creatBean()方法的方法接口，T为返回值类型
 */
@FunctionalInterface
public interface ObjectFactory<T> {
	/**
	 * 返回此工厂管理的对象的实例
	 */
	T getObject() throws BeansException;
}

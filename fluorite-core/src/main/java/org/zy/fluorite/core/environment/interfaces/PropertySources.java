package org.zy.fluorite.core.environment.interfaces;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @DateTime 2020年6月16日 下午4:00:01;
 * @author zy(azurite-Y);
 * @Description 定义存取多个属性源的相关方法
 */
public interface PropertySources extends Iterable<PropertySource<?>> {
	/**
	 * 返回包含属性源的序列流
	 */
	default Stream<PropertySource<?>> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * 判断指定名称的属性源是否存在
	 */
	boolean contains(String name);

	/**
	 * 根据指定的名称获得对应的属性源包装类对象
	 */
	PropertySource<?> get(String name);
}

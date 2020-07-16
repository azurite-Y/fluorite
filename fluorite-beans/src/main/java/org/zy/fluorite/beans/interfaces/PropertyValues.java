package org.zy.fluorite.beans.interfaces;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.zy.fluorite.beans.support.PropertyValue;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午8:26:02;
 * @Description
 */
public interface PropertyValues extends Iterable<PropertyValue> {
	
	@Override
	default Iterator<PropertyValue> iterator() {
		return Arrays.asList(getPropertyValues()).iterator();
	}

	/**
	 * 返回属性值上的拆分器
	 */
	@Override
	default Spliterator<PropertyValue> spliterator() {
		return Spliterators.spliterator(getPropertyValues(), 0);
	}

	/**
	 * 返回包含属性值的序列流
	 */
	default Stream<PropertyValue> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * 返回此对象中包含的PropertyValue对象的数组
	 */
	PropertyValue[] getPropertyValues();

	/**
	 * 返回具有给定名称的属性值（如果有）
	 */
	PropertyValue getPropertyValue(String propertyName);

	/**
	 * 更新或设置新的属性.返回如果没有更改，则为空属性值
	 */
	PropertyValues changesSince(PropertyValues old);

	/**
	 * 判断此属性是否有属性值
	 */
	boolean contains(String propertyName);

	/**
	 * 判断此持有者是否完全不包含任何PropertyValue对象
	 */
	boolean isEmpty();
}

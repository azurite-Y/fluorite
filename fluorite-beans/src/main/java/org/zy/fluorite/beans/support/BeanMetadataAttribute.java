package org.zy.fluorite.beans.support;

import org.zy.fluorite.core.interfaces.BeanMetadataElement;
import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午12:50:51;
 * @Description 存储注解的一对属性名和属性值
 */
public class BeanMetadataAttribute implements  BeanMetadataElement {
	private final String name;
	private final Object value;
	private Object source;

	public BeanMetadataAttribute(String name, Object value) {
		Assert.notNull(name, "name不能为null");
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return this.name;
	}

	public Object getValue() {
		return this.value;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	@Override
	public Object getSource() {
		return this.source;
	}
	
	@Override
	public String toString() {
		return "metadata attribute '" + this.name + "'";
	}
}

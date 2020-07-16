package org.zy.fluorite.beans.support;

import java.io.Serializable;

import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午8:27:25;
 * @Description
 */
@SuppressWarnings("serial")
public class PropertyValue extends BeanMetadataAttributeAccessor implements Serializable {
    /** 属性名 */
	private final String name;
	/** 属性值 */
	private final Object value;
	/** 可选择的 */
	private boolean optional = false;
	/** 标识是否已经过类型转换 */
	private boolean converted = false;
	/** 类型转换之后的属性值 */
	private Object convertedValue;
	/** 包可见字段，标识是否需要类型转换 */
	volatile Boolean conversionNecessary;
	/** 包可见字段，存储已解析的属性路径令牌 */
	transient volatile Object resolvedTokens;

	public PropertyValue(String name, Object value) {
		Assert.notNull(name, "属性名不能为null");
		this.name = name;
		this.value = value;
	}

	public PropertyValue(PropertyValue original) {
		Assert.notNull(original, "PropertyValue对象不能为null");
		this.name = original.getName();
		this.value = original.getValue();
		this.optional = original.isOptional();
		this.converted = original.converted;
		this.convertedValue = original.convertedValue;
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		setSource(original.getSource());
		copyAttributesFrom(original);
	}

	/**
	 * 为原始的PropertyValue对象创建一个包含新属性值的副本
	 */
	public PropertyValue(PropertyValue original, Object newValue) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = newValue;
		this.optional = original.isOptional();
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		setSource(original);
		copyAttributesFrom(original);
	}

	public String getName() {
		return this.name;
	}

	public Object getValue() {
		return this.value;
	}

	/**
	 * 返回原始的PropertyValue实例
	 */
	public PropertyValue getOriginalPropertyValue() {
		PropertyValue original = this;
		Object source = getSource();
		while (source instanceof PropertyValue && source != original) {
			original = (PropertyValue) source;
			source = original.getSource();
		}
		return original;
	}

	/**
	 * 设置此值是否为可选值，即当目标类上不存在相应的属性时将被忽略
	 */
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	/**
	 * 返回此值是否为可选值，即当目标类上不存在相应的属性时将被忽略
	 */
	public boolean isOptional() {
		return this.optional;
	}

	public synchronized boolean isConverted() {
		return this.converted;
	}

	public synchronized void setConvertedValue(Object value) {
		this.converted = true;
		this.convertedValue = value;
	}

	public synchronized Object getConvertedValue() {
		return this.convertedValue;
	}

	@Override
	public String toString() {
		return "bean property '" + this.name + "'";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (optional ? 1231 : 1237);
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyValue other = (PropertyValue) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (optional != other.optional)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}

package org.zy.fluorite.beans.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.zy.fluorite.core.interfaces.BeanMetadataElement;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午11:59:59;
 * @Description 构造函数参数值的持有者
 */
public class ConstructorArgumentValues {

	/**
	 * 存储参数下标和ValueHolder对象的映射
	 */
	private final Map<Integer, ValueHolder> indexedArgumentValues = new LinkedHashMap<>();

	public ConstructorArgumentValues() {
	}

	public ConstructorArgumentValues(ConstructorArgumentValues original) {
		addArgumentValues(original);
	}


	/**
	 * 将所有给定的参数值复制到此对象中，使用单独的holderInstance保持这些值独立于原始对象.
	 */
	public void addArgumentValues(ConstructorArgumentValues other) {
		if (other != null) {
			other.indexedArgumentValues.forEach(
				(index, argValue) -> addOrMergeIndexedArgumentValue(index, argValue.copy())
			);
		}
	}


	/**
	 * 在构造函数参数列表中为给定索引添加参数值
	 */
	public void addIndexedArgumentValue(int index, Object value) {
		addIndexedArgumentValue(index, new ValueHolder(value));
	}

	/**
	 * 在构造函数参数列表中为给定索引添加参数值
	 */
	public void addIndexedArgumentValue(int index, Object value, String type) {
		addIndexedArgumentValue(index, new ValueHolder(value, type));
	}

	/**
	 * 在构造函数参数列表中为给定索引添加参数值
	 */
	public void addIndexedArgumentValue(int index, ValueHolder newValue) {
		Assert.isTrue(index >= 0, "索引不能为负整数");
		Assert.notNull(newValue, "ValueHolder must not be null");
		addOrMergeIndexedArgumentValue(index, newValue);
	}

	/**
	 * Add an argument value for the given index in the constructor argument list,
	 * merging the new value (typically a collection) with the current value
	 * if demanded: see {@link org.springframework.beans.Mergeable}.
	 * @param key the index in the constructor argument list
	 * @param newValue the argument value in the form of a ValueHolder
	 */
	private void addOrMergeIndexedArgumentValue(Integer key, ValueHolder newValue) {
		ValueHolder currentValue = this.indexedArgumentValues.get(key);
		if (currentValue == null ) {
			this.indexedArgumentValues.put(key, newValue);
		}
	}

	/**
	 * 检查是否为给定索引注册了参数值
	 */
	public boolean hasIndexedArgumentValue(int index) {
		return this.indexedArgumentValues.containsKey(index);
	}

	/**
	 * 获取构造函数参数列表中给定索引的参数值
	 */
	public ValueHolder getIndexedArgumentValue(int index, Class<?> requiredType) {
		return getIndexedArgumentValue(index, requiredType, null);
	}

	/**
	 * 获取构造函数参数列表中给定索引的参数值
	 */
	public ValueHolder getIndexedArgumentValue(int index, Class<?> requiredType, String requiredName) {
		Assert.isTrue(index >= 0, "index不可为负整数");
		ValueHolder valueHolder = this.indexedArgumentValues.get(index);
		if (valueHolder != null &&	(valueHolder.getType() == null || (requiredType != null && ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) &&
				(valueHolder.getName() == null || "".equals(requiredName) ||	(requiredName != null && requiredName.equals(valueHolder.getName())))) {
			return valueHolder;
		}
		return null;
	}

	public Map<Integer, ValueHolder> getIndexedArgumentValues() {
		return Collections.unmodifiableMap(this.indexedArgumentValues);
	}


	/**
	 * 查找与构造函数参数列表中给定索引相对应的参数值
	 */
	public ValueHolder getArgumentValue(int index, Class<?> requiredType) {
		return getArgumentValue(index, requiredType, null, null);
	}

	/**
	 * 查找与构造函数参数列表中给定索引相对应的参数值
	 */
	public ValueHolder getArgumentValue(int index, Class<?> requiredType, String requiredName) {
		return getArgumentValue(index, requiredType, requiredName, null);
	}

	/**
	 * 查找与构造函数参数列表中给定索引相对应的参数值
	 */
	public ValueHolder getArgumentValue(int index, Class<?> requiredType, String requiredName, Set<ValueHolder> usedValueHolders) {
		Assert.isTrue(index >= 0, "索引不可为负整数");
		return getIndexedArgumentValue(index, requiredType, requiredName);
	}

	/**
	 * 返回此实例中保留的参数值的数目，同时计算索引参数值和通用参数值.
	 */
	public int getArgumentCount() {
		return (this.indexedArgumentValues.size());
	}

	/**
	 * 判断参数持有者集合是否为空集
	 */
	public boolean isEmpty() {
		return (this.indexedArgumentValues.isEmpty());
	}

	/**
	 * 删除所有参数值。
	 */
	public void clear() {
		this.indexedArgumentValues.clear();
	}
	
	/**
	 * 参数值持有者 
	 */
	public static class ValueHolder implements BeanMetadataElement {
		/** 候选参数值 */
		private Object value;
		/** 候选参数值 */
		private String type;
		/** 参数名称 */
		private String name;
		/** 存储参数的ValueHolder对象 */
		private Object source;
		/** 是否经过类型转换 */
		private boolean converted = false;
		/** 经过类型转换之后的参数值 */
		private Object convertedValue;

		public ValueHolder(Object value) {
			this.value = value;
		}

		public ValueHolder(Object value, String type) {
			this.value = value;
			this.type = type;
		}

		public ValueHolder(Object value, String type, String name) {
			this.value = value;
			this.type = type;
			this.name = name;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public Object getValue() {
			return this.value;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getType() {
			return this.type;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public void setSource(Object source) {
			this.source = source;
		}

		@Override
		public Object getSource() {
			return this.source;
		}

		/**
		 * 返回此持有者是否已包含转换后的值（true），或者是否仍需要转换该值（false）.
		 */
		public synchronized boolean isConverted() {
			return this.converted;
		}

		/**
		 * 设置类型转换之后的构造器参数值.
		 */
		public synchronized void setConvertedValue(Object value) {
			this.converted = (value != null);
			this.convertedValue = value;
		}

		/**
		 * 在处理类型转换之后，返回构造函数参数的转换值.
		 */
		public synchronized Object getConvertedValue() {
			return this.convertedValue;
		}

		/**
		 * 创建此值持有人的副本
		 */
		public ValueHolder copy() {
			ValueHolder copy = new ValueHolder(this.value, this.type, this.name);
			copy.setSource(this.source);
			return copy;
		}
	}
}

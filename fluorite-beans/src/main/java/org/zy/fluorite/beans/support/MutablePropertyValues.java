package org.zy.fluorite.beans.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;

import org.zy.fluorite.beans.interfaces.PropertyValues;
import org.zy.fluorite.core.interfaces.Mergeable;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午12:00:06;
 * @Description
 */
@SuppressWarnings("serial")
public class MutablePropertyValues implements PropertyValues,Serializable {
	
	private final List<PropertyValue> propertyValueList;

	/**
	 * 通过后处理注册的属性名
	 */
	private Set<String> processedProperties;

	private volatile boolean converted = false;

	public MutablePropertyValues() {
		this.propertyValueList = new ArrayList<>(0);
	}

	/**
	 * 深度复制构造函数。保证PropertyValue引用是独立的，
	 * 尽管它不能深入复制当前由单个PropertyValue对象引用的对象
	 */
	public MutablePropertyValues(PropertyValues original) {
		if (original != null) {
			PropertyValue[] pvs = original.getPropertyValues();
			this.propertyValueList = new ArrayList<>(pvs.length);
			for (PropertyValue pv : pvs) {
				this.propertyValueList.add(new PropertyValue(pv));
			}
		}
		else {
			this.propertyValueList = new ArrayList<>(0);
		}
	}

	/**
	 * 从映射构造新的MutablePropertyValues对象
	 */
	public MutablePropertyValues(Map<?, ?> original) {
		if (original != null) {
			this.propertyValueList = new ArrayList<>(original.size());
			original.forEach((attrName, attrValue) -> this.propertyValueList.add(
					new PropertyValue(attrName.toString(), attrValue)));
		}
		else {
			this.propertyValueList = new ArrayList<>(0);
		}
	}

	public MutablePropertyValues(List<PropertyValue> propertyValueList) {
		this.propertyValueList =	(propertyValueList != null ? propertyValueList : new ArrayList<>());
	}

	public List<PropertyValue> getPropertyValueList() {
		return this.propertyValueList;
	}

	public int size() {
		return this.propertyValueList.size();
	}

	public MutablePropertyValues addPropertyValues(PropertyValues other) {
		if (other != null) {
			PropertyValue[] pvs = other.getPropertyValues();
			for (PropertyValue pv : pvs) {
				// 保存独立的PropertyValue对象
				addPropertyValue(new PropertyValue(pv));
			}
		}
		return this;
	}

	public MutablePropertyValues addPropertyValues(Map<?, ?> other) {
		if (other != null) {
			other.forEach((attrName, attrValue) -> addPropertyValue(
					new PropertyValue(attrName.toString(), attrValue)));
		}
		return this;
	}

	public MutablePropertyValues addPropertyValue(PropertyValue pv) {
		for (int i = 0; i < this.propertyValueList.size(); i++) {
			PropertyValue currentPv = this.propertyValueList.get(i);
			if (currentPv.getName().equals(pv.getName())) {
				pv = mergeIfRequired(pv, currentPv);
				setPropertyValueAt(pv, i);
				return this;
			}
		}
		this.propertyValueList.add(pv);
		return this;
	}

	public void addPropertyValue(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
	}

	public MutablePropertyValues add(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
		return this;
	}

	/**
	 * 修改此中保存的PropertyValue对象对象，索引从0开始
	 */
	public void setPropertyValueAt(PropertyValue pv, int i) {
		this.propertyValueList.set(i, pv);
	}

	/**
	 * 如果支持并启用合并，则将提供的“new”PropertyValue的值与当前PropertyValue的值合并。
	 */
	private PropertyValue mergeIfRequired(PropertyValue newPv, PropertyValue currentPv) {
		Object value = newPv.getValue();
		if (value instanceof Mergeable) {
			Mergeable mergeable = (Mergeable) value;
			if (mergeable.isMergeEnabled()) {
				Object merged = mergeable.merge(currentPv.getValue());
				return new PropertyValue(newPv.getName(), merged);
			}
		}
		return newPv;
	}

	public void removePropertyValue(PropertyValue pv) {
		this.propertyValueList.remove(pv);
	}

	public void removePropertyValue(String propertyName) {
		this.propertyValueList.remove(getPropertyValue(propertyName));
	}


	@Override
	public Iterator<PropertyValue> iterator() {
		return Collections.unmodifiableList(this.propertyValueList).iterator();
	}

	@Override
	public Spliterator<PropertyValue> spliterator() {
		return Spliterators.spliterator(this.propertyValueList, 0);
	}

	@Override
	public Stream<PropertyValue> stream() {
		return this.propertyValueList.stream();
	}

	@Override
	public PropertyValue[] getPropertyValues() {
		return this.propertyValueList.toArray(new PropertyValue[0]);
	}

	@Override
	public PropertyValue getPropertyValue(String propertyName) {
		for (PropertyValue pv : this.propertyValueList) {
			if (pv.getName().equals(propertyName)) {
				return pv;
			}
		}
		return null;
	}

	/**
	 * 获得指定参数名的参数值
	 */
	public Object get(String propertyName) {
		PropertyValue pv = getPropertyValue(propertyName);
		return (pv != null ? pv.getValue() : null);
	}

	@Override
	public PropertyValues changesSince(PropertyValues old) {
		MutablePropertyValues changes = new MutablePropertyValues();
		if (old == this) {
			return changes;
		}

		for (PropertyValue newPv : this.propertyValueList) {
			PropertyValue pvOld = old.getPropertyValue(newPv.getName());
			if (pvOld == null || !pvOld.equals(newPv)) {
				changes.addPropertyValue(newPv);
			}
		}
		return changes;
	}

	@Override
	public boolean contains(String propertyName) {
		return (getPropertyValue(propertyName) != null ||
				(this.processedProperties != null && this.processedProperties.contains(propertyName)));
	}

	@Override
	public boolean isEmpty() {
		return this.propertyValueList.isEmpty();
	}


	/**
	 * 注册属性名
	 */
	public void registerProcessedProperty(String propertyName) {
		if (this.processedProperties == null) {
			this.processedProperties = new HashSet<>(4);
		}
		this.processedProperties.add(propertyName);
	}

	public void clearProcessedProperty(String propertyName) {
		if (this.processedProperties != null) {
			this.processedProperties.remove(propertyName);
		}
	}

	public void setConverted() {
		this.converted = true;
	}

	public boolean isConverted() {
		return this.converted;
	}


	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof MutablePropertyValues &&
				this.propertyValueList.equals(((MutablePropertyValues) other).propertyValueList)));
	}

	@Override
	public int hashCode() {
		return this.propertyValueList.hashCode();
	}

	@Override
	public String toString() {
		return "MutablePropertyValues [propertyValueList=" + propertyValueList + ", processedProperties="
				+ processedProperties + ", converted=" + converted + "]";
	}

}

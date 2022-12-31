package org.zy.fluorite.boot.util;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.zy.fluorite.boot.interfaces.MultiValueMap;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description {@link MultiValueMap} 的简单实现，包装 {@link LinkedHashMap} ，在 {@link LinkedList} 中存储多个值。
 * <p>
 * 这个Map实现通常不是线程安全的。它主要是为从请求对象中公开的数据结构而设计的，仅用于单个线程中。
 * 
 * @param <K> - key 类型
 * @param <V> - value元素类型
 */
public class LinkedMultiValueMap <K, V> extends MultiValueMapAdapter<K, V> implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2662108200506690747L;

	
	/**
	 * Create a new LinkedMultiValueMap that wraps a {@link LinkedHashMap}.
	 */
	public LinkedMultiValueMap() {
		super(new LinkedHashMap<>());
	}

	/**
	 * 创建一个新的包装 {@link LinkedHashMap} 的LinkedMultiValueMap。
	 * 
	 * @param initialCapacity - 初始容量
	 */
	public LinkedMultiValueMap(int initialCapacity) {
		super(new LinkedHashMap<>(initialCapacity));
	}

	/**
	 * 复制构造函数:创建一个新的LinkedMultiValueMap，与指定的Map具有相同的映射。
	 * 注意，这将是一个浅拷贝;它的值保存列表条目将被重用，因此不能被独立修改。
	 * 
	 * @param otherMap - 其映射将被放置在此映射中的映射
	 * 
	 * @see #clone()
	 * @see #deepCopy()
	 */
	public LinkedMultiValueMap(Map<K, List<V>> otherMap) {
		super(new LinkedHashMap<>(otherMap));
	}


	/**
	 * 创建此Map的深度副本
	 * 
	 * @return 该Map的副本，包括每个值持有列表条目的副本(始终为每个条目使用一个独立的可修改的 {@link LinkedList})，遵循MultiValueMap.addAll语义
	 * 
	 * @see #addAll(MultiValueMap)
	 * @see #clone()
	 */
	public LinkedMultiValueMap<K, V> deepCopy() {
		LinkedMultiValueMap<K, V> copy = new LinkedMultiValueMap<>(size());
		forEach((key, values) -> copy.put(key, new LinkedList<>(values)));
		return copy;
	}

	/**
	 * 创建此Map的常规副本
	 * 
	 * @return 这个Map的浅拷贝，按照标准 {@code Map.put} 语义重用这个Map的值保存列表条目(即使有些条目是共享的或不可修改的)
	 * 
	 * @see #put(Object, List)
	 * @see #putAll(Map)
	 * @see LinkedMultiValueMap#LinkedMultiValueMap(Map)
	 * @see #deepCopy()
	 */
	@Override
	public LinkedMultiValueMap<K, V> clone() {
		return new LinkedMultiValueMap<>(this);
	}
}

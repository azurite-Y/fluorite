package org.zy.fluorite.core.interfaces;

import java.util.List;
import java.util.Map;


/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description 存储多个值的 {@link Map } 接口的扩展
 */
public interface MultiValueMap<K, V> extends Map<K, List<V>> {
	/**
	 * 返回给定键的第一个值
	 * 
	 * @param key - 给定键
	 * @return 指定键的第一个值，如果没有，则为null
	 */
	V getFirst(K key);

	/**
	 * 将给定的单个值添加到给定键的当前值列表中
	 * 
	 * @param key - 给定键
	 * @param value - 要添加的值
	 */
	void add(K key, V value);

	/**
	 * 将给定列表的所有值添加到给定键的当前值列表中
	 * 
	 * @param key - 给定键
	 * @param values - 要添加的值
	 */
	void addAll(K key, List<? extends V> values);

	/**
	 * 将给定 {@code MultiValueMap} 的所有值添加到当前值
	 * 
	 * @param values - 要添加的值
	 */
	void addAll(MultiValueMap<K, V> values);

	/**
	 * 仅当映射不 {@link #containsKey(Object) 包含} 给定键时，才 {@link #add(Object, Object) 添加} 给定值
	 * 
	 * @param key - 给定键
	 * @param values - 要添加的值
	 */
	default void addIfAbsent(K key, V value) {
		if (!containsKey(key)) {
			add(key, value);
		}
	}

	/**
	 * 在给定键下设置给定的单个值
	 * 
	 * @param key - 给定键
	 * @param values - 要设置的值
	 */
	void set(K key, V value);

	/**
	 * 设置给定的值
	 * 
	 * @param values - 给定值
	 */
	void setAll(Map<K, V> values);

	/**
	 * 返回一个包含该 {@code MultiValueMap} 中第一个值的 {@code Map}
	 * 
	 * @return 此映射的单个值表示
	 */
	Map<K, V> toSingleValueMap();
}

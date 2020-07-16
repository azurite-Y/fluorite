package org.zy.fluorite.core.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @DateTime 2020年6月16日 下午5:21:02;
 * @author zy(azurite-Y);
 * @Description 容器工具类
 */
public class CollectionUtils {
	
	/** 
	 * 将指定的可变参数存储到List容器中并返回之
	 * @param <T>
	 * @param t
	 * @return 存储所有元素的ArrayList容器
	 */
	@SafeVarargs
	public static <T> List<T> asList(T... t) {
		List<T> list = new ArrayList<>();
		for (T t2 : t) {
			list.add(t2);
		}
		return list;
	}
	
	/** 
	 * 将指定的Set容器元素存储到为List容器中并返回之
	 * @param <T>
	 * @param t
	 * @return 存储所有元素的ArrayList容器
	 */
	public static <T> List<T> asList(Set<T> set) {
		List<T> list = new ArrayList<>();
		for (Iterator<T> iterator = set.iterator(); iterator.hasNext();) {
			list.add(iterator.next());
		}
		return list;
	}
	
	/** 
	 * 将指定的可变参数存储到Set容器中并返回之
	 * @param <T>
	 * @param t
	 * @return 存储所有元素的LinkedHashSet容器
	 */
	@SafeVarargs
	public static <T> Set<T> asSet(T... t) {
		Set<T> list = new LinkedHashSet<>();
		for (T t2 : t) {
			list.add(t2);
		}
		return list;
	}
}

package org.zy.fluorite.beans.support;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.zy.fluorite.core.subject.OrderComparator;
import org.zy.fluorite.core.utils.AnnotationUtils;
import org.zy.fluorite.core.utils.CollectionUtils;

/**
 * @DateTime 2020年6月18日 下午2:54:20;
 * @author zy(azurite-Y);
 * @Description
 */
public class AnnotationAwareOrderComparator extends OrderComparator {
	public static final AnnotationAwareOrderComparator INSTANCE = new AnnotationAwareOrderComparator();
	
	/**
	 * 首先假定指定对象实现了Order接口，若为实现则检查其类头是否标注了@Order注解。
	 * 若未找到有效值则返回null
	 */
	@Override
	protected Integer findOrder(Object obj) {
		Integer order = super.findOrder(obj);
		if (order != null) {
			return order;
		}
		return  AnnotationUtils.findOrderFromAnnotation(obj);
	}
	
	/**
	 * 检查是否标注@Priority注解。通常在多个匹配但只返回一个对象的情况下选择一个对象。
	 * @param obj
	 * @return
	 */
	@Override
	public Integer getPriority(Object obj) {
		if (obj instanceof Class) {
			return AnnotationUtils.findPriorityFromAnnotation((Class<?>) obj);
		}
		return AnnotationUtils.findPriorityFromAnnotation(obj);
	}

	public static void sort(List<?> list) {
		if (list.size() > 1) {
			list.sort(INSTANCE);
		}
	}
	
	public static <T> void sort(Set<T> set) {
		if (set.size() > 1) {
			List<T> asList = CollectionUtils.asList(set);
			sort(asList);
		}
	}

	public static void sort(Object[] array) {
		if (array.length > 1) {
			Arrays.sort(array, INSTANCE);
		}
	}

	public static void sortIfNecessary(Object value) {
		if (value instanceof Object[]) {
			sort((Object[]) value);
		}
		else if (value instanceof List) {
			sort((List<?>) value);
		}
	}
}

package org.zy.fluorite.core.subject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.interfaces.PriorityOrdered;
import org.zy.fluorite.core.utils.ObjectUtils;

/**
 * @DateTime 2020年6月18日 下午2:41:04;
 * @author zy(azurite-Y);
 * @Description Order接口排序
 */
public class OrderComparator implements Comparator<Object> {

	public static final OrderComparator INSTANCE = new OrderComparator();

	@Override
	public int compare(Object o1, Object o2) {
		return doCompare(o1, o2, null);
	}

	/**
	 * 
	 * @param o1
	 * @param o2
	 * @param sourceProvider - 封装获取order值的方法
	 * @return
	 */
	private int doCompare(Object o1, Object o2, OrderSourceProvider sourceProvider) {
		boolean p1 = (o1 instanceof PriorityOrdered);
		boolean p2 = (o2 instanceof PriorityOrdered);
		if (p1 && !p2) {
			return -1;
		} else if (p2 && !p1) {
			return 1;
		}

		int i1 = getOrder(o1, sourceProvider);
		int i2 = getOrder(o2, sourceProvider);
		return Integer.compare(i1, i2);
	}

	protected int getOrder(Object obj) {
		if (obj != null) {
			Integer order = findOrder(obj);
			if (order != null) {
				return order;
			}
		}
		return Ordered.LOWEST_PRECEDENCE;
	}

	private int getOrder(Object obj, OrderSourceProvider sourceProvider) {
		Integer order = null;
		if (obj != null && sourceProvider != null) {
			Object orderSource = sourceProvider.getOrderSource(obj);
			if (orderSource != null) {
				if (orderSource.getClass().isArray()) {
					Object[] sources = ObjectUtils.toObjectArray(orderSource);
					for (Object source : sources) {
						order = findOrder(source);
						if (order != null) {
							break;
						}
					}
				} else {
					order = findOrder(orderSource);
				}
			}
		}
		return (order != null ? order : getOrder(obj));
	}

	protected Integer findOrder(Object obj) {
		return (obj instanceof Ordered ? ((Ordered) obj).getOrder() : null);
	}

	
	public static void sort(List<?> list) {
		if (list.size() > 1) {
			list.sort(INSTANCE);
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
	
	/** 为给定对象提供order源的策略接口 */
	@FunctionalInterface
	public interface OrderSourceProvider {
		/**
		 * 返回指定对象的订单源，即应检查订单值以替换给定对象的对象。 也可以是order源对象的数组。 如果返回的对象没有指示任何顺序，则比较器将返回到检查原始对象
		 * 
		 * @param obj
		 * @return
		 */
		Object getOrderSource(Object obj);
	}

	public Integer getPriority(Object obj) {
		return null;
	}
}

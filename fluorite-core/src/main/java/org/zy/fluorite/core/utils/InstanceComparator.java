package org.zy.fluorite.core.utils;

import java.util.Comparator;

/**
 * @DateTime 2020年7月6日 下午2:06:44;
 * @author zy(azurite-Y);
 * @Description 此排序器根据创建时使用的Class<?>[]中的元素顺序进行排序
 */
public class InstanceComparator<T> implements Comparator<T> {
	private final Class<?>[] instanceOrder;

	public InstanceComparator(Class<?>... instanceOrder) {
		Assert.notNull(instanceOrder, "'instanceOrder' array must not be null");
		this.instanceOrder = instanceOrder;
	}

	@Override
	public int compare(T o1, T o2) {
		int i1 = getOrder(o1);
		int i2 = getOrder(o2);
		return (Integer.compare(i1, i2));
	}

	private int getOrder(T object) {
		if (object != null) {
			for (int i = 0; i < this.instanceOrder.length; i++) {
				if (this.instanceOrder[i].isInstance(object)) {
					return i;
				}
			}
		}
		return this.instanceOrder.length;
	}
}

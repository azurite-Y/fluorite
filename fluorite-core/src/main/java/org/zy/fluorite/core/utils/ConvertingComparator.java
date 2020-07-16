package org.zy.fluorite.core.utils;

import java.util.Comparator;

import org.zy.fluorite.core.interfaces.function.ActiveFunction;

/**
 * @DateTime 2020年7月6日 下午2:22:29;
 * @author zy(azurite-Y);
 * @Description 排序器和排序元素提取逻辑的组合持有者
 * @param <S> 排序元素持有对象，此Comparator实现的compare方法接收的参数
 * @param <T> 需要排序的对象，内部排序器的compare方法接收的参数
 */
public class ConvertingComparator<S, T> implements Comparator<S> {
	private final Comparator<T> comparator;
	private final ActiveFunction<T,S> function;
	
	
	/**
	 * @param comparator - 排序器，Comparator接口实现
	 * @param function - 排序元素提取逻辑
	 */
	public ConvertingComparator(Comparator<T> comparator, ActiveFunction<T , S> function) {
		super();
		Assert.notNull(comparator,"'comparator'-排序器不能为null");
		Assert.notNull(function,"'function'-排序元素提取逻辑不能为null");
		this.comparator = comparator;
		this.function = function;
	}

	@Override
	public int compare(S o1, S o2) {
		T t1 = null;
		T t2 = null;
		try {
			t1 = this.function.active(o1);
			t2 = this.function.active(o2);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return this.comparator.compare(t1, t2);
	}

}

package org.zy.fluorite.core.interfaces;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午8:52:53;
 * @Description 定义对象合并操作方法的接口
 */
public interface Mergeable {
	/**
	 * 当前对象是否是可合并的
	 * @return
	 */
	boolean isMergeEnabled();

	/**
	 * 将当前值集与提供的对象的值集合并。
	 * 提供的对象被视为父对象，被调用方的值集中的值必须覆盖提供的对象的值
	 */
	Object merge(Object parent);
}

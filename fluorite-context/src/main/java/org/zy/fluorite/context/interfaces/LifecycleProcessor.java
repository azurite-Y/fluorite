package org.zy.fluorite.context.interfaces;

/**
 * @DateTime 2020年6月17日 下午2:18:05;
 * @author zy(azurite-Y);
 * @Description 在ApplicationContext中处理生命周期bean的策略接口
 */
public interface LifecycleProcessor extends Lifecycle {
	/**
	 * 上下文刷新通知，例如自动启动组件
	 */
	void onRefresh();

	/**
	 * 上下文关闭阶段的通知，例如自动停止组件
	 */
	void onClose();
}

package org.zy.fluorite.context.interfaces;

/**
 * @DateTime 2020年6月17日 下午1:35:57;
 * @author zy(azurite-Y);
 * @Description 定义启动/停止生命周期方法的通用接口控制，控制这方面的典型用例是控制异步处理。
 *     注意：此接口并不意味着特定的自动启动语义。考虑为此目的实施SmartLifecycle
 */
public interface Lifecycle {
	void start();

	void stop();

	boolean isRunning();
}

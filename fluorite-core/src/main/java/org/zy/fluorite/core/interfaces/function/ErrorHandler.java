package org.zy.fluorite.core.interfaces.function;

/**
 * @DateTime 2020年6月18日 下午1:55:17;
 * @author zy(azurite-Y);
 * @Description 处理错误的策略。这对于异步执行已提交给任务计划程序的任务时发生的handlingerror尤其有用。
 * 在这种情况下，可能无法将错误发送给原始调用者。
 */
@FunctionalInterface
public interface ErrorHandler {
	/**
	 * 处理给定的错误，可能会将其作为致命异常重新引发
	 */
	void handleError(Throwable t);
}

package org.zy.fluorite.boot.interfaces;

/**
 * @dateTime 2022年12月24日;
 * @author zy(azurite-Y);
 * @description 用于处理启动失败的策略
 */
@FunctionalInterface
public interface FailureHandler {

	/**
	 * 始终中止的 {@link FailureHandler}
	 */
	FailureHandler NONE = (failure) -> Outcome.ABORT;

	/**
	 * 处理运行失败。实现可能会阻塞，例如等待特定文件更新。
	 * @param failure - 异常
	 * @return 结果
	 */
	Outcome handle(Throwable failure);

	/**
	 * 处理程序的各种结果
	 */
	enum Outcome {

		/** 中止重新启动 */
		ABORT,

		/** 请重试重新启动应用程序 */
		RETRY

	}

}

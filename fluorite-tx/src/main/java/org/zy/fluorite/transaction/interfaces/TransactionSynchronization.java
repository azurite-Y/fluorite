package org.zy.fluorite.transaction.interfaces;

import java.io.Flushable;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 事务同步回调接口
 */
public interface TransactionSynchronization extends Flushable {
	/** 正确提交时的完成状态 */
	int STATUS_COMMITTED = 0;

	/** 正常回滚时的完成状态 */
	int STATUS_ROLLED_BACK = 1;

	/** 启发式混合完成或系统错误情况下的完成状态 */
	int STATUS_UNKNOWN = 2;

	/**
	 * 暂停此同步
	 */
	default void suspend() {
	}

	/**
	 * 继续此同步。如果管理任何资源，则应将资源重新绑定到TransactionSynchronizationManager
	 */
	default void resume() {
	}

	@Override
	default void flush() {
	}

	/**
	 * 在事务提交之前（在“完成之前”）调用
	 */
	default void beforeCommit(boolean readOnly) {
	}

	/**
	 * 在事务提交/回滚之前调用。可以在事务完成之前执行资源清理
	 * 此方法将在beforeCommit之后调用，即使beforeCommit引发异常也是如此
	 */
	default void beforeCompletion() {
	}

	/**
	 * 在事务提交后调用。可以在主事务成功提交后立即执行进一步的操作
	 */
	default void afterCommit() {
	}

	/**
	 * 在事务提交/回滚后调用。可以在事务完成后执行资源清理
	 * @param status
	 */
	default void afterCompletion(int status) {
	}
}

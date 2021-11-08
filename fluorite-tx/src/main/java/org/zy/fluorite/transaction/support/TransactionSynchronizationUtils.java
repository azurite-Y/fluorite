package org.zy.fluorite.transaction.support;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.transaction.interfaces.TransactionSynchronization;

/**
 * @DateTime 2021年9月15日;
 * @author zy(azurite-Y);
 * @Description 事务同步工具类
 */
public abstract class TransactionSynchronizationUtils {
	private static final Logger logger = LoggerFactory.getLogger(TransactionSynchronizationUtils.class);

	/**
	 * 在所有当前注册的同步上触发刷新回调
	 */
	public static void triggerFlush() {
		for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
			synchronization.flush();
		}
	}

	/**
	 * 在对所有当前注册的同步对象触发 {@code beforeCommit } 回调
	 * @param readOnly
	 */
	public static void triggerBeforeCommit(boolean readOnly) {
		List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
		if (synchronizations != null) {
			for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
				synchronization.beforeCommit(readOnly);
			}
		}
	}

	/**
	 * 在对所有当前注册的同步对象触发 {@code afterCommit } 回调
	 */
	public static void triggerAfterCommit() {
		List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
		if (synchronizations != null) {
			for (TransactionSynchronization synchronization : synchronizations) {
				synchronization.afterCommit();
			}
		}
	}

	/**
	 * 在对所有当前注册的同步对象触发 {@code afterCommit } 回调
	 */
	public static void triggerBeforeCompletion() {
		for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
			try {
				synchronization.beforeCompletion();
			} catch (Throwable tsex) {
				logger.error("TransactionSynchronization.beforeCompletion引发异常", tsex);
			}
		}
	}

	/**
	 * 在对所有当前注册的同步对象触发 {@code afterCompletion } 回调
	 */
	public static void invokeAfterCompletion(int completionStatus ,List<TransactionSynchronization> synchronizations) {
		if (synchronizations != null) {
			for (TransactionSynchronization synchronization : synchronizations) {
				try {
					synchronization.afterCompletion(completionStatus);
				} catch (Throwable tsex) {
					logger.error("TransactionSynchronization.afterCompletion引发异常", tsex);
				}
			}
		}
	}
}

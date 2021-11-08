package org.zy.fluorite.transaction.interfaces;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 事务当前状态的通用表示形式
 */
public interface TransactionExecution {
	/**
	 * 当前事务是否为新事务
	 * @return
	 */
	boolean isNewTransaction();

	/**
	 * 仅设置事务回滚
	 */
	void setRollbackOnly();

	/**
	 * 返回事务是否已标记为仅回滚
	 * @return
	 */
	boolean isRollbackOnly();

	/**
	 * 返回此事务是否已完成，即是否已提交或回滚
	 * @return
	 */
	boolean isCompleted();
}

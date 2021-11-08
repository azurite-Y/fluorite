package org.zy.fluorite.transaction.support;

import org.zy.fluorite.transaction.exception.NestedTransactionNotSupportedException;
import org.zy.fluorite.transaction.exception.TransactionException;
import org.zy.fluorite.transaction.exception.TransactionUsageException;
import org.zy.fluorite.transaction.interfaces.SavepointManager;
import org.zy.fluorite.transaction.interfaces.TransactionStatus;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description
 */
public abstract class AbstractTransactionStatus  implements TransactionStatus {
	/** 是否仅回滚 */
	private boolean rollbackOnly = false;

	/** 是否已完成 */
	private boolean completed = false;

	/** 保存点对象 */
	private Object savepoint;
	
	@Override
	public Object createSavepoint() throws TransactionException {
		return getSavepointManager().createSavepoint();
	}
	
	@Override
	public void rollbackToSavepoint(Object savepoint) throws TransactionException {
		getSavepointManager().rollbackToSavepoint(savepoint);
	}
	
	@Override
	public void releaseSavepoint(Object savepoint) throws TransactionException {
		getSavepointManager().releaseSavepoint(savepoint);
	}

	/**
	 * 获取保存点管理器
	 * @return
	 */
	protected SavepointManager getSavepointManager() {
		throw new NestedTransactionNotSupportedException("此事务不支持保存点");
	}
	
	/**
	 * 创建保存点并为事务保留它
	 * @throws TransactionException
	 */
	public void createAndHoldSavepoint() throws TransactionException {
		setSavepoint(getSavepointManager().createSavepoint());
	}

	/**
	 * 回滚到为事务保留的保存点，然后立即释放该保存点
	 * @throws TransactionException
	 */
	public void rollbackToHeldSavepoint() throws TransactionException {
		Object savepoint = getSavepoint();
		if (savepoint == null) {
			throw new TransactionUsageException("无法回滚到保存点-没有与当前事务关联的保存点");
		}
		getSavepointManager().rollbackToSavepoint(savepoint);
		getSavepointManager().releaseSavepoint(savepoint);
		setSavepoint(null);
	}

	/**
	 * 释放为事务保留的保存点
	 * @throws TransactionException
	 */
	public void releaseHeldSavepoint() throws TransactionException {
		Object savepoint = getSavepoint();
		if (savepoint == null) {
			throw new TransactionUsageException("无法释放保存点-没有与当前事务关联的保存点");
		}
		getSavepointManager().releaseSavepoint(savepoint);
		setSavepoint(null);
	}
	
	@Override
	public void flush() {}
	
	@Override
	public void setRollbackOnly() {
		this.rollbackOnly = true;
	}
	@Override
	public boolean isRollbackOnly() {
		return (isLocalRollbackOnly() || isGlobalRollbackOnly());
	}
	public boolean isLocalRollbackOnly() {
		return this.rollbackOnly;
	}
	/**
	 * 返回事务是否在内部标记为rollback-only
	 * @return
	 */
	public boolean isGlobalRollbackOnly() {
		return false;
	}
	public void setCompleted() {
		this.completed = true;
	}
	@Override
	public boolean isCompleted() {
		return this.completed;
	}
	@Override
	public boolean hasSavepoint() {
		return (this.savepoint != null);
	}
	protected void setSavepoint(Object savepoint) {
		this.savepoint = savepoint;
	}
	protected Object getSavepoint() {
		return this.savepoint;
	}
}

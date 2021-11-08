package org.zy.fluorite.transaction.support;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;

import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ReflectionUtils;
import org.zy.fluorite.transaction.exception.NestedTransactionNotSupportedException;
import org.zy.fluorite.transaction.interfaces.SavepointManager;
import org.zy.fluorite.transaction.interfaces.SmartTransactionObject;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description
 */
public class DefaultTransactionStatus extends AbstractTransactionStatus {
	private final Object transaction;

	private final boolean newTransaction;

	private final boolean newSynchronization;

	private final boolean readOnly;

	/** 暂停资源对象 */
	private final Object suspendedResources;

	public DefaultTransactionStatus(Object transaction, boolean newTransaction, boolean newSynchronization,	boolean readOnly, Object suspendedResources) {
		this.transaction = transaction;
		this.newTransaction = newTransaction;
		this.newSynchronization = newSynchronization;
		this.readOnly = readOnly;
		this.suspendedResources = suspendedResources;
	}
	public Object getTransaction() {
		Assert.notNull(transaction, "没有可使用的事务");
		return transaction;
	}
	public boolean hasTransaction() {
		return (this.transaction != null);
	}
	public boolean isNewTransaction() {
		return (hasTransaction() && this.newTransaction);
	}
	public boolean isNewSynchronization() {
		return newSynchronization;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public Object getSuspendedResources() {
		return suspendedResources;
	}
	
	@Override
	public boolean isGlobalRollbackOnly() {
		return ((this.transaction instanceof SmartTransactionObject) &&	((SmartTransactionObject) this.transaction).isRollbackOnly());
	}

	@Override
	protected SavepointManager getSavepointManager() {
		Object transaction = this.transaction;
		if (!(transaction instanceof SavepointManager)) {
			throw new NestedTransactionNotSupportedException("此事务不支持保存点，by：" + this.transaction);
		}
		return (SavepointManager) transaction;
	}
	
	/**
	 * 返回基础事务是否实现了SavepointManagerinterface并因此支持保存点
	 * @return
	 */
	public boolean isTransactionSavepointManager() {
		return (this.transaction instanceof SavepointManager);
	}
	
	@Override
	public void flush() {
		if (this.transaction instanceof SmartTransactionObject) {
			((SmartTransactionObject) this.transaction).flush();
		}
	}
}

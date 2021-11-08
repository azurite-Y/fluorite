package org.zy.fluorite.transaction.dataSource;

import java.sql.SQLException;
import java.sql.Savepoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.transaction.exception.CannotCreateTransactionException;
import org.zy.fluorite.transaction.exception.NestedTransactionNotSupportedException;
import org.zy.fluorite.transaction.exception.TransactionException;
import org.zy.fluorite.transaction.exception.TransactionSystemException;
import org.zy.fluorite.transaction.exception.TransactionUsageException;
import org.zy.fluorite.transaction.interfaces.SavepointManager;
import org.zy.fluorite.transaction.interfaces.SmartTransactionObject;

/**
 * @DateTime 2021年9月15日;
 * @author zy(azurite-Y);
 * @Description 用于支持jdbc的事务对象的方便基类。可以包含一个带有JDBC连接的ConnectionHolder，并基于该ConnectionHolder实现SavepointManager接口
 */
public abstract class JdbcTransactionObjectSupport implements SavepointManager, SmartTransactionObject {
	private static final Logger logger = LoggerFactory.getLogger(JdbcTransactionObjectSupport.class);

	private ConnectionHolder connectionHolder;

	/** 之前的隔离级别 */
	private Integer previousIsolationLevel;

	private boolean readOnly = false;

	private boolean savepointAllowed = false;


	public void setConnectionHolder(ConnectionHolder connectionHolder) {
		this.connectionHolder = connectionHolder;
	}

	public ConnectionHolder getConnectionHolder() {
		Assert.notNull(this.connectionHolder, "没有可用的ConnectionHolder");
		return this.connectionHolder;
	}

	public boolean hasConnectionHolder() {
		return (this.connectionHolder != null);
	}

	/**
	 * 设置保留之前的隔离级别(如果有的话)。
	 */
	public void setPreviousIsolationLevel(Integer previousIsolationLevel) {
		this.previousIsolationLevel = previousIsolationLevel;
	}

	/**
	 * 返回之前的隔离级别(如果有的话)
	 */
	public Integer getPreviousIsolationLevel() {
		return this.previousIsolationLevel;
	}

	/**
	 * 设置当前事务为只读状态
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return this.readOnly;
	}

	/**
	 * 设置该事务中是否允许保存点。默认为false
	 */
	public void setSavepointAllowed(boolean savepointAllowed) {
		this.savepointAllowed = savepointAllowed;
	}

	/**
	 * 返回该事务中是否允许保存点
	 */
	public boolean isSavepointAllowed() {
		return this.savepointAllowed;
	}

	@Override
	public void flush() {}
	
	@Override
	public Object createSavepoint() throws TransactionException {
		ConnectionHolder conHolder = getConnectionHolderForSavepoint();
		try {
			if (!conHolder.supportsSavepoints()) {
				throw new NestedTransactionNotSupportedException("无法创建嵌套事务，因为JDBC驱动程序不支持保存点");
			}
			if (conHolder.isRollbackOnly()) {
				throw new CannotCreateTransactionException("无法为已标记为只回滚的事务创建保存点");
			}
			return conHolder.createSavepoint();
		} catch (SQLException ex) {
			throw new CannotCreateTransactionException("无法拆机JDBC保存点", ex);
		}
	}

	/**
	 * 回滚到给定的JDBC 3.0保存点
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	@Override
	public void rollbackToSavepoint(Object savepoint) throws TransactionException {
		ConnectionHolder conHolder = getConnectionHolderForSavepoint();
		try {
			conHolder.getConnection().rollback((Savepoint) savepoint);
			conHolder.resetRollbackOnly();
		} catch (Throwable ex) {
			throw new TransactionSystemException("无法回滚到JDBC保存点", ex);
		}
	}

	/**
	 * 发布给定的JDBC 3.0保存点
	 * @see java.sql.Connection#releaseSavepoint
	 */
	@Override
	public void releaseSavepoint(Object savepoint) throws TransactionException {
		ConnectionHolder conHolder = getConnectionHolderForSavepoint();
		try {
			// 重置jdbc连接创建的保存点 
			conHolder.getConnection().releaseSavepoint((Savepoint) savepoint);
		} catch (Throwable ex) {
			DebugUtils.logFromTransaction(logger, "不能显式地释放JDBC保存点，case: " + ex);
		}
	}

	protected ConnectionHolder getConnectionHolderForSavepoint() throws TransactionException {
		if (!isSavepointAllowed()) {
			throw new NestedTransactionNotSupportedException("事务管理器不允许嵌套事务");
		}
		if (!hasConnectionHolder()) {
			throw new TransactionUsageException("当不公开JDBC事务时，无法创建嵌套事务");
		}
		return getConnectionHolder();
	}
}

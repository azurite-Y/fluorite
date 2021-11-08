package org.zy.fluorite.transaction.dataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.transaction.interfaces.ConnectionHandle;
import org.zy.fluorite.transaction.support.ResourceHolderSupport;

/**
 * @DateTime 2021年9月15日;
 * @author zy(azurite-Y);
 * @Description JDBC连接保持类
 */
public class ConnectionHolder extends ResourceHolderSupport {
	/** 保存点名称前缀 */
	public static final String SAVEPOINT_NAME_PREFIX = "SAVEPOINT_";

	private ConnectionHandle connectionHandle;

	private Connection currentConnection;

	private boolean transactionActive = false;

	private Boolean savepointsSupported;

	private int savepointCounter = 0;

	public ConnectionHolder(ConnectionHandle connectionHandle) {
		Assert.notNull(connectionHandle, "ConnectionHandle 不能为null");
		this.connectionHandle = connectionHandle;
	}

	/**
	 * 为给定的JDBC Connection创建一个新的ConnectionHolder，使用SimpleConnectionHandle包装它，假设没有正在进行的事务
	 */
	public ConnectionHolder(Connection connection) {
		this.connectionHandle = new SimpleConnectionHandle(connection);
	}

	/**
	 * 为给定的JDBC Connection创建一个新的ConnectionHolder，并用SimpleConnectionHandle包装它
	 * @param connection - 要保持的JDBC连接
	 * @param transactionActive - 给定的Connection是否涉及正在进行的事务
	 * @see SimpleConnectionHandle
	 */
	public ConnectionHolder(Connection connection, boolean transactionActive) {
		this(connection);
		this.transactionActive = transactionActive;
	}

	public ConnectionHandle getConnectionHandle() {
		return this.connectionHandle;
	}

	protected boolean hasConnection() {
		return (this.connectionHandle != null);
	}

	protected void setTransactionActive(boolean transactionActive) {
		this.transactionActive = transactionActive;
	}

	protected boolean isTransactionActive() {
		return this.transactionActive;
	}


	/**
	 * 使用给定的Connection覆盖现有的Connection。如果给定null，则重置现有的Connection
	 */
	protected void setConnection(Connection connection) {
		if (this.currentConnection != null) {
			if (this.connectionHandle != null) {
				this.connectionHandle.releaseConnection(this.currentConnection);
			}
			this.currentConnection = null;
		}
		if (connection != null) {
			this.connectionHandle = new SimpleConnectionHandle(connection);
		} else {
			this.connectionHandle = null;
		}
	}

	/**
	 * 返回由这个ConnectionHolder持有的当前连接。这将是相同的连接，直到在ConnectionHolder上调用releasedgets，它将重置被持有的连接，并根据需要获取一个新的连接
	 * @see ConnectionHandle#getConnection()
	 * @see #released()
	 */
	public Connection getConnection() {
		Assert.notNull(this.connectionHandle, "connectionHandle 是必须的");
		if (this.currentConnection == null) {
			this.currentConnection = this.connectionHandle.getConnection();
		}
		return this.currentConnection;
	}

	/**
	 * 返回是否支持JDBC 3.0保存点
	 * @throws SQLException - JDBC驱动程序抛出
	 */
	public boolean supportsSavepoints() throws SQLException {
		if (this.savepointsSupported == null) {
			this.savepointsSupported = getConnection().getMetaData().supportsSavepoints();
		}
		return this.savepointsSupported;
	}

	/**
	 * 为当前连接创建一个新的JDBC 3.0保存点，使用生成的保存点名称，这些名称对连接来说是唯一的
	 * @return 新的保存点
	 * @throws SQLException - JDBC驱动程序抛出
	 */
	public Savepoint createSavepoint() throws SQLException {
		this.savepointCounter++;
		return getConnection().setSavepoint(SAVEPOINT_NAME_PREFIX + this.savepointCounter);
	}

	/**
	 * 释放由这个ConnectionHolder持有的当前连接
	 */
	@Override
	public void released() {
		super.released();
		if (!isOpen() && this.currentConnection != null) {
			if (this.connectionHandle != null) {
				this.connectionHandle.releaseConnection(this.currentConnection);
			}
			this.currentConnection = null;
		}
	}


	@Override
	public void clear() {
		super.clear();
		this.transactionActive = false;
		this.savepointsSupported = null;
		this.savepointCounter = 0;
	}

}

package org.zy.fluorite.transaction.dataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.transaction.exception.CannotGetJdbcConnectionException;
import org.zy.fluorite.transaction.interfaces.ConnectionProxy;
import org.zy.fluorite.transaction.interfaces.SmartDataSource;
import org.zy.fluorite.transaction.interfaces.TransactionDefinition;
import org.zy.fluorite.transaction.interfaces.TransactionSynchronization;
import org.zy.fluorite.transaction.support.TransactionSynchronizationManager;

/**
 * @DateTime 2021年9月16日;
 * @author zy(azurite-Y);
 * @Description
 */
public class DataSourceUtils {
	protected static Logger logger = LoggerFactory.getLogger(DataSourceUtils.class);

	/**
	 * 根据传入的事务对象设置jdbc连接的的只读标识和事务隔离级别
	 * @param con
	 * @param definition
	 * @return
	 * @throws SQLException
	 */
	public static Integer prepareConnectionForTransaction(Connection con, TransactionDefinition definition) throws SQLException {
		Assert.notNull(con, "Connection 不能为null");

		// 设置只读标识
		if (definition != null && definition.isReadOnly()) {
			try {
				DebugUtils.logFromTransaction(logger, "设置jdbc连接只读");
				con.setReadOnly(true);
			} catch (SQLException | RuntimeException ex) {
				Throwable exToCheck = ex;
				while (exToCheck != null) {
					if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
						throw ex;
					}
					exToCheck = exToCheck.getCause();
				}
				DebugUtils.logFromTransaction(logger, "无法设置jdbc连接只读状态");
			}
		}

		// 应用特定的隔离级别（如有）
		Integer previousIsolationLevel = null;
		if (definition != null && definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			DebugUtils.logFromTransaction(logger, "更改JDBC连接的隔离级别为  [" + definition.getIsolationLevel() + "], by ：" +con);
			int currentIsolation = con.getTransactionIsolation();
			if (currentIsolation != definition.getIsolationLevel()) {
				previousIsolationLevel = currentIsolation;
				con.setTransactionIsolation(definition.getIsolationLevel());
			}
		}
		return previousIsolationLevel;
	}

	/**
	 * 关闭从给定数据源获得的给定连接
	 * @param con - 要关闭的连接
	 * @param dataSource 从中获取连接的数据源（可能为null）
	 * @see #getConnection
	 */
	public static void releaseConnection(Connection con, DataSource dataSource) {
		try {
			doReleaseConnection(con, dataSource);
		} catch (SQLException ex) {
			logger.debug("无法关闭 JDBC Connection", ex);
		} catch (Throwable ex) {
			logger.debug("关闭JDBC连接时出现意外异常", ex);
		}
	}

	/**
	 * 在事务之后重置给定的连接（关于只读标志和隔离级别）
	 * @param con
	 * @param previousIsolationLevel
	 * @param resetReadOnly
	 */
	public static void resetConnectionAfterTransaction(Connection con, Integer previousIsolationLevel, boolean resetReadOnly) {
		Assert.notNull(con, "'Connection' 不能为null");
		try {
			if (previousIsolationLevel != null) {
				DebugUtils.logFromTransaction(logger, "重置连接[" + con + "] 的事务隔离级别为：" + previousIsolationLevel);
				con.setTransactionIsolation(previousIsolationLevel);
			}

			if (resetReadOnly) {
				DebugUtils.logFromTransaction(logger, "重置连接[" + con + "] 的仅回滚标识");
				con.setReadOnly(false);
			}
		} catch (Throwable ex) {
			logger.debug("无法重置JDBC连接", ex);
		}
	}

	/**
	 * 实际关闭从给定数据源获取的给定连接
	 * @param con
	 * @param dataSource
	 * @throws SQLException
	 */
	public static void doReleaseConnection(Connection con, DataSource dataSource) throws SQLException {
		if (con == null) {
			return;
		}
		if (dataSource != null) {
			ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
			if (conHolder != null && connectionEquals(conHolder, con)) {
				// 事务性连接无需关闭
				conHolder.released();
				return;
			}
		}
		doCloseConnection(con, dataSource);
	}

	/**
	 * 关闭连接，除非SmartDataSource不希望我们这样做
	 * @param con
	 * @param dataSource
	 * @throws SQLException
	 */
	public static void doCloseConnection(Connection con, DataSource dataSource) throws SQLException {
		if (!(dataSource instanceof SmartDataSource) || ((SmartDataSource) dataSource).shouldClose(con)) {
			con.close();
		}
	}

	/**
	 * 确定给定的两个连接是否相等
	 * @param conHolder
	 * @param passedInCon
	 * @return
	 */
	private static boolean connectionEquals(ConnectionHolder conHolder, Connection passedInCon) {
		if (!conHolder.hasConnection()) {
			return false;
		}
		Connection heldCon = conHolder.getConnection();
		return (heldCon == passedInCon || heldCon.equals(passedInCon) || getTargetConnection(heldCon).equals(passedInCon));
	}

	/**
	 * 从可能的 ConnectionProxy 中获得原始的 Connection
	 * @param con
	 * @return
	 * @see ConnectionProxy#getTargetConnection()
	 */
	public static Connection getTargetConnection(Connection con) {
		Connection conToUse = con;
		while (conToUse instanceof ConnectionProxy) {
			conToUse = ((ConnectionProxy) conToUse).getTargetConnection();
		}
		return conToUse;
	}

	/**
	 * 从jdbc连接中获得唯一的数据库标识id
	 * @param dataSource
	 * @return
	 */
	public static String getdatabaseIdForDataSource(DataSource dataSource) {
		Connection con = null;
		try {
			con = dataSource.getConnection();
			DatabaseMetaData metaData = con.getMetaData();
			return metaData.getDatabaseProductName();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
				}
			}
		}
		return null;
	}

	/**
	 * 尝试从事务环境中获得jdbc连接，若没有则重新从数据源获取
	 * @param dataSource
	 * @return
	 */
	public static Connection getConnection(DataSource dataSource) {
		try {
			return doGetConnection(dataSource);
		} catch (SQLException | IllegalStateException ex) {
			throw new CannotGetJdbcConnectionException("获取JDBC连接失败", ex);
		}
	}

	public static Connection doGetConnection(DataSource dataSource) throws SQLException {
		Assert.notNull(dataSource, "参数 'DataSource'不能为null");

		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		if (conHolder != null && (conHolder.hasConnection() || conHolder.isSynchronizedWithTransaction())) {
			conHolder.requested();
			if (!conHolder.hasConnection()) {
				DebugUtils.logFromTransaction(logger, "从数据源恢复 JDBC Connection");
				conHolder.setConnection(fetchConnection(dataSource));
			}
			return conHolder.getConnection();
		}

		DebugUtils.logFromTransaction(logger, "从数据源恢复 JDBC Connection");
		Connection con = fetchConnection(dataSource);

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			try {
				ConnectionHolder holderToUse = conHolder;
				if (holderToUse == null) {
					holderToUse = new ConnectionHolder(con);
				} else {
					holderToUse.setConnection(con);
				}
				holderToUse.requested();
				TransactionSynchronizationManager.registerSynchronization(new ConnectionSynchronization(holderToUse, dataSource));
				holderToUse.setSynchronizedWithTransaction(true);
				if (holderToUse != conHolder) {
					TransactionSynchronizationManager.bindResource(dataSource, holderToUse);
				}
			} catch (RuntimeException ex) {
				releaseConnection(con, dataSource);
				throw ex;
			}
		}
		return con;
	}

	/**
	 * 实际上从给定的数据源获取连接
	 * @param dataSource
	 * @return
	 * @throws SQLException
	 */
	private static Connection fetchConnection(DataSource dataSource) throws SQLException {
		Connection con = dataSource.getConnection();
		if (con == null) {
			throw new IllegalStateException("从DataSource获取连接为null: " + dataSource);
		}
		return con;
	}

	/**
	 * 判断给定的数据源和jdbc连接是否已注册到事务环境中
	 * @param connection
	 * @param dataSource
	 * @return 若事务环境中存在则返回true，反之则返回false
	 */
	public static boolean isConnectionTransactional(Connection connection, DataSource dataSource) {
		if (dataSource == null) {
			return false;
		}
		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		return (conHolder != null && connectionEquals(conHolder, connection));
	}
	
	/**
	 * 
	 * @author Azurite-Y
	 *
	 */
	private static class ConnectionSynchronization implements TransactionSynchronization {

		private final ConnectionHolder connectionHolder;

		private final DataSource dataSource;

		private boolean holderActive = true;

		public ConnectionSynchronization(ConnectionHolder connectionHolder, DataSource dataSource) {
			this.connectionHolder = connectionHolder;
			this.dataSource = dataSource;
		}

		@Override
		public void suspend() {
			if (this.holderActive) {
				TransactionSynchronizationManager.unbindResource(this.dataSource);
				if (this.connectionHolder.hasConnection() && !this.connectionHolder.isOpen()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
					this.connectionHolder.setConnection(null);
				}
			}
		}

		@Override
		public void resume() {
			if (this.holderActive) {
				TransactionSynchronizationManager.bindResource(this.dataSource, this.connectionHolder);
			}
		}

		@Override
		public void beforeCompletion() {
			if (!this.connectionHolder.isOpen()) {
				TransactionSynchronizationManager.unbindResource(this.dataSource);
				this.holderActive = false;
				if (this.connectionHolder.hasConnection()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
				}
			}
		}

		@Override
		public void afterCompletion(int status) {
			if (this.holderActive) {
				TransactionSynchronizationManager.unbindResourceIfPossible(this.dataSource);
				this.holderActive = false;
				if (this.connectionHolder.hasConnection()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
					this.connectionHolder.setConnection(null);
				}
			}
			this.connectionHolder.reset();
		}
	}
}

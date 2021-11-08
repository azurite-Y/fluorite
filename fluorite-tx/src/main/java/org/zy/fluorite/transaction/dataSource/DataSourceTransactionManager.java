package org.zy.fluorite.transaction.dataSource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.zy.fluorite.core.interfaces.instantiation.InitializingBean;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.transaction.exception.CannotCreateTransactionException;
import org.zy.fluorite.transaction.exception.TransactionException;
import org.zy.fluorite.transaction.exception.TransactionSystemException;
import org.zy.fluorite.transaction.interfaces.ResourceTransactionManager;
import org.zy.fluorite.transaction.interfaces.TransactionDefinition;
import org.zy.fluorite.transaction.support.AbstractPlatformTransactionManager;
import org.zy.fluorite.transaction.support.DefaultTransactionStatus;
import org.zy.fluorite.transaction.support.TransactionSynchronizationManager;
import org.zy.fluorite.transaction.support.TransactionSynchronizationUtils;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class DataSourceTransactionManager extends AbstractPlatformTransactionManager implements ResourceTransactionManager, InitializingBean{

	private DataSource dataSource;

	public DataSourceTransactionManager(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DataSourceTransactionManager() {
		setNestedTransactionAllowed(true);
	}

	/**
	 * 获取数据源以供实际使用
	 * @return
	 */
	protected DataSource obtainDataSource() {
		DataSource dataSource = getDataSource();
		Assert.notNull(dataSource, "DataSource为null");
		return dataSource;
	}

	@Override
	public void afterPropertiesSet() {
		if (getDataSource() == null) {
			throw new IllegalArgumentException("属性 'dataSource' 是必须的");
		}
	}

	@Override
	public Object getResourceFactory() {
		return obtainDataSource();
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		Connection con = null;		

		try {
			if (!txObject.hasConnectionHolder() || txObject.getConnectionHolder().isSynchronizedWithTransaction()) {
				Connection newCon = obtainDataSource().getConnection();
				DebugUtils.logFromTransaction(logger, "已获取JDBC事务的连接[" + newCon + "]");
				txObject.setConnectionHolder(new ConnectionHolder(newCon), true);
			}

			txObject.getConnectionHolder().setSynchronizedWithTransaction(true);
			con = txObject.getConnectionHolder().getConnection();

			// 设置事务隔离级别
			Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
			txObject.setPreviousIsolationLevel(previousIsolationLevel);
			// 设置只读标识
			txObject.setReadOnly(definition.isReadOnly());

			// 将自动提交切换为手动提交
			if (con.getAutoCommit()) {
				txObject.setMustRestoreAutoCommit(true);
				DebugUtils.logFromTransaction(logger, "将JDBC连接 [" + con + "] 切换为手动提交");
				con.setAutoCommit(false);
			}

			// 在连接保持对象中标记为事务开启
			txObject.getConnectionHolder().setTransactionActive(true);

			// 配置超时时间
			int timeout = determineTimeout(definition);
			if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getConnectionHolder().setTimeoutInSeconds(timeout);
			}

			// 在ThreadLocal中保存Datasource和ConnectionHolder的映射
			if (txObject.isNewConnectionHolder()) {
				TransactionSynchronizationManager.bindResource(obtainDataSource(), txObject.getConnectionHolder());
			}
		} catch (Throwable ex) {
			if (txObject.isNewConnectionHolder()) {
				DataSourceUtils.releaseConnection(con, obtainDataSource());
				txObject.setConnectionHolder(null, false);
			}
			throw new CannotCreateTransactionException("无法打开事务的JDBC连接", ex);
		}
	}

	protected int determineTimeout(TransactionDefinition definition) {
		if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
			return definition.getTimeout();
		}
		return getDefaultTimeout();
	}

	@Override
	protected Object doGetTransaction() {
		DataSourceTransactionObject txObject = new DataSourceTransactionObject();
		txObject.setSavepointAllowed(isNestedTransactionAllowed());
		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(obtainDataSource());
		txObject.setConnectionHolder(conHolder, false);
		return txObject;
	}

	@Override
	protected boolean isExistingTransaction(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		return (txObject.hasConnectionHolder() && txObject.getConnectionHolder().isTransactionActive());
	}

	@Override
	protected Object doSuspend(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		txObject.setConnectionHolder(null);
		return TransactionSynchronizationManager.unbindResource(obtainDataSource());
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		DebugUtils.logFromTransaction(logger, "提交jdbc事务，by Connection [" + con + "]");
		try {
			con.commit();
		} catch (SQLException ex) {
			super.setRollbackOnCommitFailure(true);
			throw new TransactionSystemException("无法提交 JDBC 事务", ex);
		}		
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		DebugUtils.logFromTransaction(logger, "回滚jdbc事务，by Connection：[" + con + "]");
		try {
			con.rollback();
		} catch (SQLException ex) {
			throw new TransactionSystemException("无法回滚JDBC事务", ex);
		}
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;

		if (txObject.isNewConnectionHolder()) {
			// 解绑DataSource
			TransactionSynchronizationManager.unbindResource(obtainDataSource());
		}

		// 重置连接
		Connection con = txObject.getConnectionHolder().getConnection();
		try {
			if (txObject.isMustRestoreAutoCommit()) {
				con.setAutoCommit(true);
			}
			DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel(), txObject.isReadOnly());
		} catch (Throwable ex) {
			logger.debug("无法重置JDBC连接", ex);
		}

		if (txObject.isNewConnectionHolder()) {
			DebugUtils.logFromTransaction(logger, "关闭JDBC连接 [" + con + "]");
			DataSourceUtils.releaseConnection(con, this.dataSource);
		}

		txObject.getConnectionHolder().clear();
	}

	@Override
	protected void doResume(Object transaction, Object suspendedResources) {
		TransactionSynchronizationManager.bindResource(obtainDataSource(), suspendedResources);
	}
	
	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		DebugUtils.logFromTransaction(logger, "设置 JDBC 事务 [" + txObject.getConnectionHolder().getConnection() +	"] 仅回滚");
		txObject.setRollbackOnly();
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public DataSource getDataSource() {
		return this.dataSource;
	}

	/**
	 * 数据源事务对象
	 * @author Azurite-Y
	 *
	 */
	@SuppressWarnings("unused")
	private static class DataSourceTransactionObject extends JdbcTransactionObjectSupport {

		private boolean newConnectionHolder;

		private boolean mustRestoreAutoCommit;

		public void setConnectionHolder(ConnectionHolder connectionHolder, boolean newConnectionHolder) {
			super.setConnectionHolder(connectionHolder);
			this.newConnectionHolder = newConnectionHolder;
		}

		public boolean isNewConnectionHolder() {
			return this.newConnectionHolder;
		}

		public void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit) {
			this.mustRestoreAutoCommit = mustRestoreAutoCommit;
		}

		public boolean isMustRestoreAutoCommit() {
			return this.mustRestoreAutoCommit;
		}

		public void setRollbackOnly() {
			getConnectionHolder().setRollbackOnly();
		}

		@Override
		public boolean isRollbackOnly() {
			return getConnectionHolder().isRollbackOnly();
		}

		@Override
		public void flush() {
			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				TransactionSynchronizationUtils.triggerFlush();
			}
		}
	}
}

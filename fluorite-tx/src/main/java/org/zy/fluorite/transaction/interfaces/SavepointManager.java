package org.zy.fluorite.transaction.interfaces;

import org.zy.fluorite.transaction.exception.TransactionException;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 保存点管理器
 */
public interface SavepointManager {
	/**
	 * 创建一个新的保存点。您可以通过rollbackToSavepoint回滚到特定的savepoint，并通过releaseSavepoint显式释放不再需要的savepoint
	 * @return
	 * @throws TransactionException
	 */
	Object createSavepoint() throws TransactionException;
	
	/**
	 * 回滚到给定的保存点
	 * @param savepoint
	 * @throws TransactionException
	 */
	void rollbackToSavepoint(Object savepoint) throws TransactionException;
	
	/**
	 * 显式释放给定的保存点
	 * @param savepoint
	 * @throws TransactionException
	 */
	void releaseSavepoint(Object savepoint) throws TransactionException;
}

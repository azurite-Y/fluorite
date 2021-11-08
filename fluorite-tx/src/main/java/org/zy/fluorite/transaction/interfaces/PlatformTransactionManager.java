package org.zy.fluorite.transaction.interfaces;

import org.zy.fluorite.transaction.exception.TransactionException;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 事务基础设施中心接口
 */
public interface PlatformTransactionManager extends TransactionManager {
	/**
	 * 根据指定的传播行为返回当前活动的事务或创建新事务
	 * @param definition
	 * @return
	 * @throws TransactionException
	 */
	TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;
	
	/**
	 * 提交给定事务的状态
	 * @param status
	 * @throws TransactionException
	 */
	void commit(TransactionStatus status) throws TransactionException;
	
	/**
	 * 执行给定事务的回滚
	 * @param status
	 * @throws TransactionException
	 */
	void rollback(TransactionStatus status) throws TransactionException;
}

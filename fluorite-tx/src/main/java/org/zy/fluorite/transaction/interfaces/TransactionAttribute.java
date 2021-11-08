package org.zy.fluorite.transaction.interfaces;

/**
 * @DateTime 2021年9月16日;
 * @author zy(azurite-Y);
 * @Description
 */
public interface TransactionAttribute extends TransactionDefinition {
	/**
	 * 返回与此事务属性关联的限定符值。这可用于选择相应的事务管理器来处理这个特定的事务。
	 * @return
	 */
	String getQualifier();

	/**
	 * 设置是否被指定的异常触发回滚
	 * @param ex
	 * @return 是否执行回滚
	 */
	boolean rollbackOn(Throwable ex);
}

package org.zy.fluorite.transaction.interfaces;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 静态的不可修改的事务定义
 */
final class StaticTransactionDefinition implements TransactionDefinition {

	static final StaticTransactionDefinition INSTANCE = new StaticTransactionDefinition();

	private StaticTransactionDefinition() {}
}

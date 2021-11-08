package org.zy.fluorite.transaction.annotation;

import org.zy.fluorite.transaction.interfaces.TransactionDefinition;

/**
 * @DateTime 2021年9月17日;
 * @author zy(azurite-Y);
 * @Description 事务传播行为枚举
 */
public enum Propagation {
	/**
	 * 如果当前有事务,就在事务中执行,如果当前没有事务,新建一个事务
	 */
	REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),

	/**
	 * 如果当前有事务就在事务中执行,如果当前没有事务,就在非事务状态下执行
	 */
	SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS),

	/**
	 * 必须在事务内部执行,如果当前有事务,就在事务中执行,如果没有事务则报错
	 */
	MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),

	/**
	 * 必须在事务中执行,如果当前没有事务,新建事务,如果当前有事务,把当前事务挂起
	 */
	REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),

	/**
	 * 必须在非事务下执行,如果当前没有事务,正常执行,如果当前有事务,把当前事务挂起
	 */
	NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),

	/**
	 * 必须在非事务状态下执行,如果当前没有事务,正常执行, 如果当前有事务则报错
	 */
	NEVER(TransactionDefinition.PROPAGATION_NEVER),

	/**
	 * 必须在事务状态下执行.如果没有事务,新建事务,如果当前有事务,创建一个嵌套事务
	 */
	NESTED(TransactionDefinition.PROPAGATION_NESTED);


	private final int value;


	Propagation(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
}

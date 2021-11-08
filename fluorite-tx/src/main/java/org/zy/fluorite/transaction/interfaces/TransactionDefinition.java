package org.zy.fluorite.transaction.interfaces;


/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 事务环境所需基本属性定义
 */
public interface TransactionDefinition {
	
	/* --------------------------------- 事务的传播行为 --------------------------------- */
	/**
	 * 如果当前有事务,就在事务中执行,如果当前没有事务,新建一个事务
	 */
	int PROPAGATION_REQUIRED = 0;

	/**
	 * 如果当前有事务就在事务中执行,如果当前没有事务,就在非事务状态下执行
	 */
	int PROPAGATION_SUPPORTS = 1;

	/**
	 * 必须在事务内部执行,如果当前有事务,就在事务中执行,如果没有事务则报错
	 */
	int PROPAGATION_MANDATORY = 2;

	/**
	 * 必须在事务中执行,如果当前没有事务,新建事务,如果当前有事务,把当前事务挂起
	 */
	int PROPAGATION_REQUIRES_NEW = 3;

	/**
	 * 必须在非事务下执行,如果当前没有事务,正常执行,如果当前有事务,把当前事务挂起
	 */
	int PROPAGATION_NOT_SUPPORTED = 4;

	/**
	 * 必须在非事务状态下执行,如果当前没有事务,正常执行, 如果当前有事务则报错
	 */
	int PROPAGATION_NEVER = 5;

	/**
	 * 必须在事务状态下执行.如果没有事务,新建事务,如果当前有事务,创建一个嵌套事务
	 */
	int PROPAGATION_NESTED = 6;

	/* --------------------------------- 事务的隔离行为 --------------------------------- */
	/**
	 * 默认隔离状态
	 */
	int ISOLATION_DEFAULT = -1;
	
	/**
	 * 读未提交
	 */
	int ISOLATION_READ_UNCOMMITTED = 1;
	
	/**
	 * 读已提交
	 */
	int ISOLATION_READ_COMMITTED = 2;
	
	/**
	 * 可重复读
	 */
	int ISOLATION_REPEATABLE_READ = 4;
	
	/**
	 * 串行化
	 */
	int ISOLATION_SERIALIZABLE = 8;
	
	/**
	 * 基础事务的默认超时时间（秒）【默认值：-1】
	 */
	int TIMEOUT_DEFAULT = -1;

	/**
	 * 返回具有默认值的不可修改的TransactionDefinition
	 * @return
	 */
	static TransactionDefinition withDefaults() {
		return StaticTransactionDefinition.INSTANCE;
	}
	
	/**
	 * 返回事务的传播行为
	 * @return
	 */
	default int getPropagationBehavior() {
		return PROPAGATION_REQUIRED;
	}
	
	/**
	 * 返回事务的超时时间
	 * @return
	 */
	default int getTimeout() {
		return TIMEOUT_DEFAULT;
	}
	
	/**
	 * 范湖事务的隔离级别
	 * @return
	 */
	default int getIsolationLevel() {
		return ISOLATION_DEFAULT;
	}
	
	/**
	 * 返回是否为只读事务
	 * @return
	 */
	default boolean isReadOnly() {
		return false;
	}
	
	/**
	 * 返回此事务的名称。可以为空
	 * @return
	 */
	default String getName() {return null;}
	
	/**
	 * 设置此事物的名称
	 * @param name
	 */
	default void setName(String name) {}
}

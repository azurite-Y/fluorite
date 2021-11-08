package org.zy.fluorite.transaction.interfaces;

import java.io.Flushable;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 事务状态定义
 */
public interface TransactionStatus extends TransactionExecution, SavepointManager, Flushable {
	/**
	 * 返回此事务是否在内部携带保存点
	 * @return
	 */
	boolean hasSavepoint();
	
	@Override
	void flush();
}

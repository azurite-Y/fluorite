package org.zy.fluorite.transaction.interfaces;

import java.io.Flushable;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description
 */
public interface SmartTransactionObject extends Flushable {
	/**
	 * 返回事务是否在内部标记为rollback-only
	 * @return
	 */
	boolean isRollbackOnly();
	
	@Override
	void flush();
}

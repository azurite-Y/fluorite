package org.zy.fluorite.transaction.interfaces;

/**
 * @DateTime 2021年9月15日;
 * @author zy(azurite-Y);
 * @Description
 */
public interface ResourceHolder {
	/**
	 * 重置此持有者的事务状态
	 */
	void reset();

	/**
	 * 通知此持有人其已从事务同步中解除绑定.
	 */
	void unbound();

	/**
	 * 确定该持有人是否被视为“无效”
	 */
	boolean isVoid();
}

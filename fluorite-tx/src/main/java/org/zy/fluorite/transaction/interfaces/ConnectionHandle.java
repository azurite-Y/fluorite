package org.zy.fluorite.transaction.interfaces;

import java.sql.Connection;

/**
 * @DateTime 2021年9月15日;
 * @author zy(azurite-Y);
 * @Description jdbc连接处理器
 */
public interface ConnectionHandle {
	/**
	 * 获得 JDBC Connection
	 */
	Connection getConnection();

	/**
	 * 释放传入的Connection ， 默认实现是空的，将Connection交由外部管理
	 */
	default void releaseConnection(Connection con) {
	}
}

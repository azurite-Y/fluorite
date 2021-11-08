package org.zy.fluorite.transaction.dataSource;

import java.sql.Connection;

import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.transaction.interfaces.ConnectionHandle;

/**
 * @DateTime 2021年9月15日;
 * @author zy(azurite-Y);
 * @Description ConnectionHandle接口的简单实现，包含给定的JDBC Connection
 */
public class SimpleConnectionHandle implements ConnectionHandle {
	private final Connection connection;

	public SimpleConnectionHandle(Connection connection) {
		Assert.notNull(connection, "属性'connection'不能为null");
		this.connection = connection;
	}

	@Override
	public Connection getConnection() {
		return this.connection;
	}

	@Override
	public String toString() {
		return "SimpleConnectionHandle: " + this.connection;
	}
}

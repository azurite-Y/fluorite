package org.zy.fluorite.transaction.interfaces;

import java.sql.Connection;

import javax.sql.DataSource;

/**
 * @DateTime 2021年9月18日;
 * @author zy(azurite-Y);
 * @Description
 */
public interface ConnectionProxy extends DataSource {

	/**
	 * 从代理中获得原始的jdbc连接对象
	 * @return
	 */
	Connection getTargetConnection();
}

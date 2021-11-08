package org.zy.fluorite.transaction.interfaces;

import java.sql.Connection;

import javax.sql.DataSource;

/**
 * @DateTime 2021年9月18日;
 * @author zy(azurite-Y);
 * @Description
 */
public interface SmartDataSource extends DataSource{
	/**
	 * 是否应该关闭从该数据源获取的此连接
	 * @param con
	 * @return
	 * @see java.sql.Connection#close()
	 */
	boolean shouldClose(Connection con);
}

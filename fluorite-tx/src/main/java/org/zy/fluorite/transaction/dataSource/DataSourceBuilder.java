package org.zy.fluorite.transaction.dataSource;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @DateTime 2021年10月26日;
 * @author zy(azurite-Y);
 * @Description
 */
public final class DataSourceBuilder {
	private static final Logger logger = LoggerFactory.getLogger(DataSourceBuilder.class);

	public DataSource createDataSource(String typeName, String driverClassName,String url, String username, String password) {
		DataSourceType dataSourceType = DataSourceType.forCode(typeName);
		DataSource dataSource = null;
		switch (dataSourceType) {
			case druid : 
				DruidDataSource druidDataSource = new DruidDataSource();
				druidDataSource.setDriverClassName(driverClassName);
				druidDataSource.setUrl(url);
				druidDataSource.setUsername(username);
				druidDataSource.setPassword(password);
				dataSource = druidDataSource;
				break;
			case HikariCP : 
				HikariDataSource hikariDataSource = new HikariDataSource();
				hikariDataSource.setDriverClassName(driverClassName);
				hikariDataSource.setJdbcUrl(url);
				hikariDataSource.setUsername(username);
				hikariDataSource.setPassword(password);
				dataSource = hikariDataSource;
				break;
			case dbcp : 
				BasicDataSource basicDataSource = new BasicDataSource();
				basicDataSource.setDriverClassName(driverClassName);
				basicDataSource.setUrl(url);
				basicDataSource.setUsername(username);
				basicDataSource.setPassword(password);
				dataSource = basicDataSource;
				break;
			default : 
		}
		Assert.notNull(dataSource, "创建数据源失败，未找到符合类型的数据源，by type：" + typeName
				+ "，预期的数据源类型：[HikariCP、dbcp、druid]");
		DebugUtils.logFromTransaction(logger, "--> 创建数据源成功! DataSourceType:[" + dataSourceType + "]");
		return dataSource;
	}
}

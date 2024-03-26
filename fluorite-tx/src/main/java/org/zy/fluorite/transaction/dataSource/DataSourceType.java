package org.zy.fluorite.transaction.dataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * @DateTime 2021年10月26日;
 * @author zy(azurite-Y);
 * @Description
 */
public enum DataSourceType {
	HikariCP("com.zaxxer.hikari.HikariDataSource"),
	
	dbcp("org.apache.commons.dbcp2.BasicDataSource"),
	
	druid("com.alibaba.druid.pool.DruidDataSource");
	
	public final String TYPE_NAME;
	private static Map<String,DataSourceType> typeLookup = new HashMap<>();

	static {
		for (DataSourceType type : DataSourceType.values()) {
			typeLookup.put(type.TYPE_NAME, type);
		}
	}
	
	DataSourceType(String typeName) {
		this.TYPE_NAME = typeName;
	}
	
	public static DataSourceType forCode(String typeName)  {
		return typeLookup.get(typeName);
	}
}

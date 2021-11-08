package org.zy.fluorite.autoconfigure.transaction;

import javax.sql.DataSource;

import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.annotation.Configuration;
import org.zy.fluorite.core.annotation.Order;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.interfaces.EnvironmentAware;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.transaction.dataSource.DataSourceBuilder;

/**
 * @DateTime 2021年10月26日;
 * @author zy(azurite-Y);
 * @Description
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataSourceConfiguration implements EnvironmentAware{
	private ConfigurableEnvironment environment;
	
	private static final String DATASOURCE_TYPE = "fluorite.datasource.type";
	private static final String DRIVER_CLASS_NAME = "fluorite.datasource.driverClassName";
	private static final String URL = "fluorite.datasource.url";
	private static final String USERNAME = "fluorite.datasource.username";
	private static final String PASSWORD = "fluorite.datasource.password";
	
	@Bean
	public DataSource dataSource() throws ClassNotFoundException {
		String dataSourceType = environment.getProperty(DATASOURCE_TYPE);
		String driverClassName = environment.getProperty(DRIVER_CLASS_NAME);
		String url = environment.getProperty(URL);
		String username = environment.getProperty(USERNAME);
		String password = environment.getProperty(PASSWORD);
		DataSourceBuilder dataSourceBuilder = new DataSourceBuilder();
		return dataSourceBuilder.createDataSource(dataSourceType, driverClassName, url, username, password);
	}

	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		this.environment = environment;
	}

}

package org.zy.fluorite.autoconfigure.transaction;

import javax.sql.DataSource;

import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.annotation.Configuration;
import org.zy.fluorite.core.annotation.Order;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.transaction.dataSource.DataSourceTransactionManager;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description
 */
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class DataSourceTransactionManagerAutoConfiguration {

	@Bean
	DataSourceTransactionManager transactionManager(DataSource dataSource) {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		return transactionManager;
	}

}

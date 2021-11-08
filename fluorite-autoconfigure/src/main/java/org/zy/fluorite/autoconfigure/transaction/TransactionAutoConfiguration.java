package org.zy.fluorite.autoconfigure.transaction;

import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.annotation.Configuration;
import org.zy.fluorite.transaction.annotation.AnnotationTransactionAttributeSource;
import org.zy.fluorite.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.zy.fluorite.transaction.interceptor.TransactionInterceptor;
import org.zy.fluorite.transaction.interfaces.TransactionAttributeSource;
import org.zy.fluorite.transaction.interfaces.TransactionManager;

/**
 * @DateTime 2021年9月17日;
 * @author zy(azurite-Y);
 * @Description
 */
@Configuration
public class TransactionAutoConfiguration {
	@Bean
	public TransactionAttributeSource transactionAttributeSource() {
		return new AnnotationTransactionAttributeSource();
	}

	@Bean
	public TransactionInterceptor transactionInterceptor(TransactionAttributeSource transactionAttributeSource, TransactionManager transactionManager) {
		TransactionInterceptor interceptor = new TransactionInterceptor();
		interceptor.setTransactionAttributeSource(transactionAttributeSource);
		interceptor.setTransactionManager(transactionManager);
		return interceptor;
	}
	
	@Bean
	public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(TransactionAttributeSource transactionAttributeSource, TransactionInterceptor transactionInterceptor) {
		BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
		advisor.setTransactionAttributeSource(transactionAttributeSource);
		advisor.setAdvice(transactionInterceptor);
		return advisor;
	}
}

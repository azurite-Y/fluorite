package org.zy.fluorite.transaction.interceptor;

import org.zy.fluorite.aop.interfaces.Pointcut;
import org.zy.fluorite.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.zy.fluorite.transaction.interfaces.TransactionAttributeSource;

/**
 * @DateTime 2021年9月16日;
 * @author zy(azurite-Y);
 * @Description 由TransactionAttributeSource驱动的Advisor，用于封装事务性方法的事务advisor bean
 */
@SuppressWarnings("serial")
public class BeanFactoryTransactionAttributeSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {
	private TransactionAttributeSource transactionAttributeSource;
	
	private final TransactionAttributeSourcePointcut pointcut = new TransactionAttributeSourcePointcut() {
		@Override
		protected TransactionAttributeSource getTransactionAttributeSource() {
			return transactionAttributeSource;
		}
	};
	
	public void setTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource) {
		this.transactionAttributeSource = transactionAttributeSource;
	}
	
	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}
}

package org.zy.fluorite.transaction.interceptor;

import java.io.Serializable;

import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;
import org.zy.fluorite.aop.proxy.JdkDynamicAopProxy;
import org.zy.fluorite.aop.support.DefaultAdvisorChainFactory;

/**
 * @DateTime 2021年9月17日;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class TransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor, Serializable {


	/**
	 * 由JdkDynamicAopProxy的invoke方法调用.getInterceptorsAndDynamicInterceptionAdvice(Advised, Method, Class<?>) 方法
	 * 从BeanFactoryTransactionAttributeSourceAdvisor中获得本Advice实现
	 * @see JdkDynamicAopProxy#invoke(Object, Method, Object[])
	 * @see DefaultAdvisorChainFactory#getInterceptorsAndDynamicInterceptionAdvice(Advised, Method, Class)
	 * @see BeanFactoryTransactionAttributeSourceAdvisor#getAdvice()
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		return invokeWithinTransaction(invocation.getMethod(), invocation.getThis().getClass(), invocation::proceed);
	}
}

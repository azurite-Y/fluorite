package org.zy.fluorite.transaction.interceptor;

import java.lang.reflect.Method;

import org.zy.fluorite.aop.interfaces.Pointcut;
import org.zy.fluorite.aop.interfaces.function.ClassFilter;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.transaction.interfaces.PlatformTransactionManager;
import org.zy.fluorite.transaction.interfaces.TransactionAttributeSource;

/**
 * @DateTime 2021年9月16日;
 * @author zy(azurite-Y);
 * @Description 事务切点类
 */
abstract class TransactionAttributeSourcePointcut implements Pointcut {
	private ClassFilter classFilter;
	
	protected TransactionAttributeSourcePointcut() {
		setClassFilter(new TransactionAttributeSourceClassFilter());
	}
	
	/**
	 * 获取基础TransactionAttributeSource（可能为空）。将由子类实现
	 * @return
	 */
	protected abstract TransactionAttributeSource getTransactionAttributeSource();

	/*
	 * @see AopUtils#canApply(Pointcut, Class<?>)为
	 */
	@Override
	public boolean matcher(Class<?> clz) {
		return getClassFilter().matches(clz);
	}

	/*
	 * 调用自 DefaultAdvisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(Advised, Method, Class<?>) 方法
	 * @see DefaultAdvisorChainFactory#getInterceptorsAndDynamicInterceptionAdvice(Advised, Method, Class)
	 */
	@Override
	public boolean matcher(Class<?> targetClass, Method method) {
		TransactionAttributeSource tas = getTransactionAttributeSource();
		return (tas != null && tas.getTransactionAttribute(method, targetClass) != null);
	}

	private class TransactionAttributeSourceClassFilter implements ClassFilter {
		@Override
		public boolean matches(Class<?> clazz) {
			if (PlatformTransactionManager.class.isAssignableFrom(clazz)) { // 排除事务设施的相关类实现
				return false;
			}
			TransactionAttributeSource tas = getTransactionAttributeSource();
			Assert.notNull(tas,"配置于 TransactionAttributeSourcePointcut 的属性 ‘TransactionAttributeSource’ 不能为null");
			return tas.isCandidateClass(clazz);
		}
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + getTransactionAttributeSource();
	}
	
	public ClassFilter getClassFilter() {
		return classFilter;
	}
	
	public void setClassFilter(ClassFilter classFilter) {
		this.classFilter = classFilter;
	}
}

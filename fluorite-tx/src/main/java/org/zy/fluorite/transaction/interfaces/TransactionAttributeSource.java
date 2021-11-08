package org.zy.fluorite.transaction.interfaces;

import java.lang.reflect.Method;

/**
 * @DateTime 2021年9月16日;
 * @author zy(azurite-Y);
 * @Description
 */
public interface TransactionAttributeSource {

	/**
	 * 判断给定类中是否有可能应用事务环绕
	 * @param targetClass
	 * @return
	 */
	default boolean isCandidateClass(Class<?> targetClass) {
		return false;
	}

	/**
	 * 返回给定方法的事务属性，如果该方法是非事务性的，则返回null
	 * @param method - 内省的方法
	 * @param targetClass - 目标类
	 * @return
	 */
	TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass);
}

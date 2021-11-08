package org.zy.fluorite.transaction.annotation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.transaction.interfaces.TransactionAttribute;
import org.zy.fluorite.transaction.interfaces.TransactionAttributeSource;

/**
 * @DateTime 2021年9月17日;
 * @author zy(azurite-Y);
 * @Description TransactionAttributeSource的抽象实现
 */
public abstract class AbstractTransactionAttributeSource implements TransactionAttributeSource {
	protected final Logger logger = LoggerFactory.getLogger(AbstractTransactionAttributeSource.class);
	
	/**
	 * 保存从属于方法的事务属性映射关系
	 */
	protected final Map<Method, TransactionAttribute> methodAttributeCache = new ConcurrentHashMap<>(1024);
	
	/**
	 * 保存从属于类的事务属性映射关系 
	 */
	protected final Map<Class<?>, TransactionAttribute> clzAttributeTemplateCache = new ConcurrentHashMap<>();

	@Override
	public boolean isCandidateClass(Class<?> targetClass) {
		boolean isCandidateClass = null != findTransactionAttribute(targetClass);
		
		// 需继续检查方法上是否标注了@Transactional注解才可完全排除此类不能进行事务环绕的可能性
		Method[] declaredMethods = targetClass.getDeclaredMethods();
		boolean candidate = false;
		for (Method method : declaredMethods) {
			if (findTransactionAttribute(method) != null && candidate == false) {
				candidate = true;
			}
		}
		return candidate ? true : isCandidateClass;
	}
	
	@Override
	public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
		TransactionAttribute transactionAttribute = methodAttributeCache.get(method);
		return transactionAttribute == null ? clzAttributeTemplateCache.get(targetClass) : transactionAttribute;
	}
	
	/**
	 * 子类需要实现这个函数来返回给定类的事务属性(如果有的话)
	 * @param clazz
	 * @return
	 */
	protected abstract TransactionAttribute findTransactionAttribute(Class<?> clazz);

	/**
	 * 子类需要实现这个函数来返回给定方法的事务属性(如果有的话)。
	 * @param method
	 * @return
	 */
	protected abstract TransactionAttribute findTransactionAttribute(Method method);
}

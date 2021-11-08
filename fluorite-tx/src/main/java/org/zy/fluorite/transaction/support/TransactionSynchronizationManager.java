package org.zy.fluorite.transaction.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.support.AnnotationAwareOrderComparator;
import org.zy.fluorite.core.subject.NamedThreadLocal;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.transaction.interfaces.ResourceHolder;
import org.zy.fluorite.transaction.interfaces.TransactionSynchronization;

/**
 * @DateTime 2021年9月15日;
 * @author zy(azurite-Y);
 * @Description 事务同步管理器，管理每个线程的资源和事务同步的中心委托。由资源管理代码使用，但不由典型应用程序代码使用
 */
public class TransactionSynchronizationManager {
	private static final Logger logger = LoggerFactory.getLogger(TransactionSynchronizationManager.class);

	/** DataSource:ConnectionHolder */
	private static final ThreadLocal<Map<Object, Object>> resources = new NamedThreadLocal<>("事务资源");

	private static final ThreadLocal<Set<TransactionSynchronization>> synchronizations = new NamedThreadLocal<>("事务同步对象");

	private static final ThreadLocal<String> currentTransactionName = new NamedThreadLocal<>("当前事务名称");

	private static final ThreadLocal<Boolean> currentTransactionReadOnly = new NamedThreadLocal<>("当前只读状态的事务");

	private static final ThreadLocal<Integer> currentTransactionIsolationLevel = new NamedThreadLocal<>("当前会隔离级别");

	private static final ThreadLocal<Boolean> actualTransactionActive =	new NamedThreadLocal<>("实际的事务激活状态");

	/**
	 * 返回绑定到当前线程的所有资源
	 * @return
	 */
	public static Map<Object, Object> getResourceMap() {
		Map<Object, Object> map = resources.get();
		return (map != null ? Collections.unmodifiableMap(map) : Collections.emptyMap());
	}

	/**
	 * 判断指定key是否绑定到当前线程下
	 * @param key
	 * @return
	 */
	public static boolean hasResource(Object key) {
		return (doGetResource(key) != null);
	}

	/**
	 * 检索绑定到当前线程的给定 key 的资源
	 */
	public static Object getResource(Object key) {
		return doGetResource(key);
	}

	/**
	 * 根据指定key获得对应的资源
	 */
	private static Object doGetResource(Object actualKey) {
		Map<Object, Object> map = resources.get();
		if (map == null) {
			return null;
		}
		Object value = map.get(actualKey);
		if (value instanceof ResourceHolder && ((ResourceHolder) value).isVoid()) {
			map.remove(actualKey);
			if (map.isEmpty()) {
				resources.remove();
			}
			value = null;
		}
		return value;
	}

	/**
	 * 绑定指定资源到当前线程
	 */
	public static void bindResource(Object key, Object value) throws IllegalStateException {
		Assert.notNull(value, "value 不能为null");
		Map<Object, Object> map = resources.get();
		if (map == null) {
			map = new HashMap<>();
			resources.set(map);
		}
		Object oldValue = map.put(key, value);
		if (oldValue instanceof ResourceHolder && ((ResourceHolder) oldValue).isVoid()) {
			oldValue = null;
		}
		if (oldValue != null) {
			throw new IllegalStateException("在线程 [" + Thread.currentThread().getName() + "]上 key ["+ key + "] 早已绑定了对应的值" + oldValue);
		}
		DebugUtils.logFromTransaction(logger, "自线程 ["+ Thread.currentThread().getName() +"]中绑定 key["+ key + "]，"+"对应的值 ["+ value +"]");
	}

	/**
	 * 从当前线程取消绑定给定key的资源
	 */
	public static Object unbindResource(Object key) throws IllegalStateException {
		Object value = doUnbindResource(key);
		if (value == null) {
			throw new IllegalStateException("在线程 [" + Thread.currentThread().getName() + "]上未找到 key ["+ key + "] 对应的值" + value);
		}
		return value;
	}

	/**
	 * 从当前线程取消绑定给定密钥的资源
	 */
	public static Object unbindResourceIfPossible(Object key) {
		return doUnbindResource(key);
	}

	/**
	 * 删除给定key绑定的资源
	 */
	private static Object doUnbindResource(Object actualKey) {
		Map<Object, Object> map = resources.get();
		if (map == null) {
			return null;
		}
		Object value = map.remove(actualKey);
		if (map.isEmpty()) {
			resources.remove();
		}
		if (value instanceof ResourceHolder && ((ResourceHolder) value).isVoid()) {
			value = null;
		}
		if (value != null) {
			DebugUtils.logFromTransaction(logger, "自线程 ["+ Thread.currentThread().getName() +"]中删除 key["+ actualKey + "]"+"对应的值 ["+ value +"]");
		}
		return value;
	}

	/**
	 * 返回当前线程的事务同步状态
	 */
	public static boolean isSynchronizationActive() {
		return (synchronizations.get() != null);
	}

	/**
	 * 激活当前线程的事务同步。在事务开始时由事务管理器调用
	 */
	public static void initSynchronization() throws IllegalStateException {
		Assert.isTrue(!isSynchronizationActive(), "无法初始化开启的事务同步");
		DebugUtils.logFromTransaction(logger, "初始化事务同步");
		synchronizations.set(new LinkedHashSet<>());
	}

	/**
	 * 为当前线程注册新事务同步
	 */
	public static void registerSynchronization(TransactionSynchronization synchronization) throws IllegalStateException {
		Assert.notNull(synchronization, "TransactionSynchronization不能为null");
		Set<TransactionSynchronization> synchs = synchronizations.get();
		if (synchs == null) { // 在注册新事物时被初始化赋值空集
			throw new IllegalArgumentException("事务同步未开启");
		}
		synchs.add(synchronization);
	}

	/**
	 * 返回当前线程的所有已注册同步的不可修改快照列表
	 */
	public static List<TransactionSynchronization> getSynchronizations() throws IllegalStateException {
		Set<TransactionSynchronization> synchs = synchronizations.get();
		if (synchs == null) {
			throw new IllegalStateException("事务同步未开启");
		}
		if (synchs.isEmpty()) {
			return Collections.emptyList();
		} else {
			List<TransactionSynchronization> sortedSynchs = new ArrayList<>(synchs);
			AnnotationAwareOrderComparator.sort(sortedSynchs);
			return Collections.unmodifiableList(sortedSynchs);
		}
	}

	/**
	 * 停用当前线程的事务同步。在事务清理时由事务管理器调用
	 */
	public static void clearSynchronization() throws IllegalStateException {
		Assert.isTrue(isSynchronizationActive(), "无法停用未激活的同步事务");
		DebugUtils.logFromTransaction(logger, "清除所有关联的事务同步对象");
		synchronizations.remove();
	}

	/**
	 * 设置当前事务的名称
	 */
	public static void setCurrentTransactionName(String name) {
		currentTransactionName.set(name);
	}

	/**
	 * 返回当前事务的名称，如果未设置，则返回null
	 */
	public static String getCurrentTransactionName() {
		return currentTransactionName.get();
	}

	/**
	 * 设置当前与线程关联的事务的只读状态
	 */
	public static void setCurrentTransactionReadOnly(boolean readOnly) {
		currentTransactionReadOnly.set(readOnly ? Boolean.TRUE : null);
	}

	/**
	 * 返回当前事务是否标记为只读
	 */
	public static boolean isCurrentTransactionReadOnly() {
		return (currentTransactionReadOnly.get() != null);
	}

	/**
	 * 设置当前与线程关联的事务隔离级别
	 * @param isolationLevel - 根据jdbc Connection常量(相当于相应的SpringTransactionDefinition常量)，选择要公开的隔离级别，或者为空来重置它
	 */
	public static void setCurrentTransactionIsolationLevel(Integer isolationLevel) {
		currentTransactionIsolationLevel.set(isolationLevel);
	}

	/**
	 * 获得当前与线程关联的事务隔离级别
	 */
	public static Integer getCurrentTransactionIsolationLevel() {
		return currentTransactionIsolationLevel.get();
	}

	/**
	 * 设置当前是否有实际事务处于活动状态。在事务开始和清理时由事务管理器调用
	 */
	public static void setActualTransactionActive(boolean active) {
		actualTransactionActive.set(active ? Boolean.TRUE : null);
	}

	/**
	 * 返回当前是否存在活动的实际事务。这表示当前线程是否与实际事务关联，而不仅仅与活动事务同步关联
	 */
	public static boolean isActualTransactionActive() {
		return (actualTransactionActive.get() != null);
	}


	/**
	 * 清除当前线程的整个事务同步状态：已注册的同步以及各种事务特征
	 */
	public static void clear() {
		synchronizations.remove();
		currentTransactionName.remove();
		currentTransactionReadOnly.remove();
		currentTransactionIsolationLevel.remove();
		actualTransactionActive.remove();
	}
}

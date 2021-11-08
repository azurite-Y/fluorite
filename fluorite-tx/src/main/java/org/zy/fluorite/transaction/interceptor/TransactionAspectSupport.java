package org.zy.fluorite.transaction.interceptor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.factory.aware.BeanFactoryAware;
import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.function.InvocationCallback;
import org.zy.fluorite.core.interfaces.instantiation.InitializingBean;
import org.zy.fluorite.core.subject.NamedThreadLocal;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.transaction.exception.TransactionSystemException;
import org.zy.fluorite.transaction.interfaces.PlatformTransactionManager;
import org.zy.fluorite.transaction.interfaces.TransactionAttribute;
import org.zy.fluorite.transaction.interfaces.TransactionAttributeSource;
import org.zy.fluorite.transaction.interfaces.TransactionManager;
import org.zy.fluorite.transaction.interfaces.TransactionStatus;

/**
 * @DateTime 2021年9月17日;
 * @author zy(azurite-Y);
 * @Description
 */
public class TransactionAspectSupport implements BeanFactoryAware, InitializingBean {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String transactionManagerBeanName;

	private TransactionManager transactionManager;

	private TransactionAttributeSource transactionAttributeSource;

	private BeanFactory beanFactory;

	private static final ThreadLocal<TransactionInfo> transactionInfoHolder = new NamedThreadLocal<>("TransactionInfo 线程缓存");

	/** beanName：TransactionManager */
	private final Map<String, TransactionManager> transactionManagerCache =	new ConcurrentHashMap<>();
	
	/** 默认 TransactionManager 实现的BeanName */
	private static final String DEFAULT_TRANSACTION_MANAGER_KEY = "dataSourceTransactionManager";

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(transactionManager,"'transactionManager'不能为null");
		Assert.notNull(transactionAttributeSource,"'transactionAttributeSource'不能为");

	}

	/**
	 * 事务调用拦截方法
	 * @param method 被调用方法
	 * @param targetClass 费用方法所属类
	 * @param invocation 回调方法对象
	 * @return 调用方法的返回值
	 * @throws Throwable 调用过程中出现异常触发
	 */
	protected Object invokeWithinTransaction(Method method, Class<?> targetClass, final InvocationCallback<Object> invocation) throws Throwable {
		TransactionAttributeSource tas = getTransactionAttributeSource();
		TransactionAttribute txAttr = (tas != null ? tas.getTransactionAttribute(method, targetClass) : null);
		
		TransactionManager tm = determineTransactionManager(txAttr);
		PlatformTransactionManager ptm = asPlatformTransactionManager(tm);

		final String joinpointIdentification = methodIdentification(method, targetClass, txAttr);
		txAttr.setName(joinpointIdentification);
		
		TransactionInfo txInfo = createTransactionIfNecessary(ptm, txAttr, joinpointIdentification);
		Object retVal;
		try {
			// 调用链中的下一个拦截器
			retVal = invocation.proceedWithInvocation();
		} catch (Throwable ex) {
			// 目标调用异常
			completeTransactionAfterThrowing(txInfo, ex);
			throw ex;
		} finally {
			cleanupTransactionInfo(txInfo);
		}
		commitTransactionAfterReturning(txInfo);
		return retVal;
	}

	/**
	 * 解析可能存在的 TransactionManager别名，以从BeanFactory中获得对应的Bean，若无则使用持有的 TransactionManager对象
	 * @param txAttr
	 * @return
	 */
	protected TransactionManager determineTransactionManager(TransactionAttribute txAttr) {
		if (txAttr == null || this.beanFactory == null) {
			return getTransactionManager();
		}

		String qualifier = txAttr.getQualifier();
		if (Assert.hasText(qualifier)) {
			return determineQualifiedTransactionManager(this.beanFactory, qualifier);
		} else if (Assert.hasText(this.transactionManagerBeanName)) {
			return determineQualifiedTransactionManager(this.beanFactory, this.transactionManagerBeanName);
		} else {
			TransactionManager defaultTransactionManager = getTransactionManager();
			if (defaultTransactionManager == null) {
				defaultTransactionManager = this.transactionManagerCache.get(DEFAULT_TRANSACTION_MANAGER_KEY);
				if (defaultTransactionManager == null) {
					defaultTransactionManager = this.beanFactory.getBean(TransactionManager.class);
					this.transactionManagerCache.putIfAbsent(
							DEFAULT_TRANSACTION_MANAGER_KEY, defaultTransactionManager);
				}
			}
			return defaultTransactionManager;
		}
	}

	/**
	 * 根据 TransactionManager Bean的别名从Bean工厂中尝试获取对应的bean
	 * @param beanFactory
	 * @param qualifier
	 * @return
	 */
	private TransactionManager determineQualifiedTransactionManager(BeanFactory beanFactory, String qualifier) {
		TransactionManager txManager = this.transactionManagerCache.get(qualifier);
		if (txManager == null) {
			if (beanFactory.containsBean(qualifier)) {
				txManager= beanFactory.getBean(qualifier, TransactionManager.class);
				// 缓存 TransactionManager 对象
				this.transactionManagerCache.putIfAbsent(qualifier, txManager);
			} else {
				throw new NoSuchBeanDefinitionException("未从BeanFactory中获得指定别名的'TransactionManager'实现，by qualifier：" + qualifier);
			}
		}
		return txManager;
	}

	/**
	 * 根据传参创建连接点标识
	 * @param method
	 * @param targetClass
	 * @param txAttr
	 * @return
	 */
	private String methodIdentification(Method method, Class<?> targetClass, TransactionAttribute txAttr) {
		return ClassUtils.getFullyQualifiedName(method);
	}

	/**
	 * 必要时根据给定的TransactionAttribute创建事务。
	 * 允许调用者通过TransactionAttributeSource执行自定义的TransactionAttribute查找
	 * @param tm
	 * @param txAttr
	 * @param joinpointIdentification
	 * @return
	 */
	protected TransactionInfo createTransactionIfNecessary(PlatformTransactionManager tm, TransactionAttribute txAttr, final String joinpointIdentification) {
		TransactionStatus status = null;
		if (txAttr != null) {
			if (tm != null) {
				status = tm.getTransaction(txAttr);
			} else {
				DebugUtils.logFromTransaction(logger, "跳过事务连接点[" + joinpointIdentification + "]，因为没有配置事务管理器");
			}
		}
		return prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
	}

	/**
	 * 为给定的属性和状态对象准备一个TransactionInfo
	 * @param tm
	 * @param txAttr
	 * @param joinpointIdentification
	 * @param status
	 * @return
	 */
	protected TransactionInfo prepareTransactionInfo(PlatformTransactionManager tm, TransactionAttribute txAttr, String joinpointIdentification, TransactionStatus status) {
		TransactionInfo txInfo = new TransactionInfo(tm, txAttr, joinpointIdentification);
		if (txAttr != null) {
			DebugUtils.logFromTransaction(logger, "为 [" + joinpointIdentification + "] 创建事务");
			txInfo.newTransactionStatus(status);
		} else {
			DebugUtils.logFromTransaction(logger, "不需要为 [" + joinpointIdentification + "] 创建事务，该方法不是事务性的");
		}
		txInfo.bindToThread();
		return txInfo;
	}

	/**
	 * 处理throwable，完成事务
	 * @param txInfo
	 * @param ex
	 */
	protected void completeTransactionAfterThrowing(TransactionInfo txInfo, Throwable ex) {
		if (txInfo != null && txInfo.getTransactionStatus() != null) {
			DebugUtils.logFromTransaction(logger, "完成 [" + txInfo.getJoinpointIdentification() +	"]事务出现异常: " + ex);
			if (txInfo.transactionAttribute != null && txInfo.transactionAttribute.rollbackOn(ex)) {
				try {
					txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());
				} catch (TransactionSystemException ex2) {
					logger.error("应用程序异常被回滚异常覆盖", ex);
					throw ex2;
				} catch (RuntimeException | Error ex2) {
					logger.error("应用程序异常被回滚异常覆盖", ex);
					throw ex2;
				}
			} else {
				try {
					txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
				} catch (TransactionSystemException ex2) {
					logger.error("应用程序异常被回滚异常覆盖", ex);
					throw ex2;
				}
				catch (RuntimeException | Error ex2) {
					logger.error("应用程序异常被回滚异常覆盖", ex);
					throw ex2;
				}
			}
		}
	}

	/**
	 * 在成功完成调用后执行，而不是在处理异常后执行。如果不创建事务，则什么也不做
	 * @param txInfo
	 */
	protected void commitTransactionAfterReturning(TransactionInfo txInfo) {
		if (txInfo != null && txInfo.getTransactionStatus() != null) {
			DebugUtils.logFromTransaction(logger, "开始提交[" + txInfo.getJoinpointIdentification() + "]事务");
			txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
		}
	}

	/**
	 * 重置 ThreadLocal之中的TransactionInfo
	 * @param txInfo
	 */
	protected void cleanupTransactionInfo(TransactionInfo txInfo) {
		if (txInfo != null) {
			txInfo.restoreThreadLocalStatus();
		}
	}

	private PlatformTransactionManager asPlatformTransactionManager(Object transactionManager) {
		if (transactionManager == null || transactionManager instanceof PlatformTransactionManager) {
			return (PlatformTransactionManager) transactionManager;
		} else {
			throw new IllegalStateException("指定的事务管理器未实现 'PlatformTransactionManager',by：" + transactionManager);
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	protected final BeanFactory getBeanFactory() {
		return this.beanFactory;
	}
	public String getTransactionManagerBeanName() {
		return transactionManagerBeanName;
	}
	public void setTransactionManagerBeanName(String transactionManagerBeanName) {
		this.transactionManagerBeanName = transactionManagerBeanName;
	}
	public TransactionManager getTransactionManager() {
		return transactionManager;
	}
	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	public TransactionAttributeSource getTransactionAttributeSource() {
		return transactionAttributeSource;
	}
	public void setTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource) {
		this.transactionAttributeSource = transactionAttributeSource;
	}

	protected static final class TransactionInfo {

		private final PlatformTransactionManager transactionManager;

		private final TransactionAttribute transactionAttribute;

		private final String joinpointIdentification;

		private TransactionStatus transactionStatus;

		private TransactionInfo oldTransactionInfo;

		public TransactionInfo(PlatformTransactionManager transactionManager,
				TransactionAttribute transactionAttribute, String joinpointIdentification) {
			this.transactionManager = transactionManager;
			this.transactionAttribute = transactionAttribute;
			this.joinpointIdentification = joinpointIdentification;
		}

		public PlatformTransactionManager getTransactionManager() {
			Assert.notNull(this.transactionManager, "属性'PlatformTransactionManager'未设置");
			return this.transactionManager;
		}

		public TransactionAttribute getTransactionAttribute() {
			return this.transactionAttribute;
		}

		/**
		 * 返回这个连接点的String表示(通常是Method调用)，用于日志记录
		 */
		public String getJoinpointIdentification() {
			return this.joinpointIdentification;
		}

		public void newTransactionStatus(TransactionStatus status) {
			this.transactionStatus = status;
		}

		public TransactionStatus getTransactionStatus() {
			return this.transactionStatus;
		}

		public boolean hasTransaction() {
			return (this.transactionStatus != null);
		}

		/**
		 * 绑定事务状态到线程中
		 */
		private void bindToThread() {
			// 暴露当前的TransactionStatus，保留任何现有的TransactionStatus，以便在此事务完成后进行恢复.
			this.oldTransactionInfo = transactionInfoHolder.get();
			transactionInfoHolder.set(this);
		}

		/**
		 * 恢复之前的事务状态（如果有的话）
		 */
		private void restoreThreadLocalStatus() {
			transactionInfoHolder.set(this.oldTransactionInfo);
		}

		@Override
		public String toString() {
			return (this.transactionAttribute != null ? this.transactionAttribute.toString() : "No transaction");
		}
	}
}

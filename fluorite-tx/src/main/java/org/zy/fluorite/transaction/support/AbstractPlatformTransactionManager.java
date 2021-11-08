package org.zy.fluorite.transaction.support;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.transaction.exception.IllegalTransactionStateException;
import org.zy.fluorite.transaction.exception.InvalidTimeoutException;
import org.zy.fluorite.transaction.exception.NestedTransactionNotSupportedException;
import org.zy.fluorite.transaction.exception.TransactionException;
import org.zy.fluorite.transaction.exception.TransactionSuspensionNotSupportedException;
import org.zy.fluorite.transaction.exception.UnexpectedRollbackException;
import org.zy.fluorite.transaction.interfaces.PlatformTransactionManager;
import org.zy.fluorite.transaction.interfaces.TransactionDefinition;
import org.zy.fluorite.transaction.interfaces.TransactionStatus;
import org.zy.fluorite.transaction.interfaces.TransactionSynchronization;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public abstract class AbstractPlatformTransactionManager implements PlatformTransactionManager, Serializable {
	protected transient Logger logger = LoggerFactory.getLogger(getClass());

	/**	始终开启事务同步 */
	public static final int SYNCHRONIZATION_ALWAYS = 0;
	/** 仅为实际事务激活事务同步 */
	public static final int SYNCHRONIZATION_ON_ACTUAL_TRANSACTION = 1;
	/** 始终不开启事务同步 */
	public static final int SYNCHRONIZATION_NEVER = 2;

	/** 事务同步标记 */
	private int transactionSynchronization = SYNCHRONIZATION_ALWAYS;

	/** 默认事务超时时间 */
	private int defaultTimeout = TransactionDefinition.TIMEOUT_DEFAULT;

	/** 是否应该在参与现有事务之前对其进行验证 */
	private boolean validateExistingTransaction;

	/** 是否允许嵌套事务 */
	private boolean nestedTransactionAllowed;

	/** 标记doCommit 触发异常是否应调用 doRollback */
	private boolean rollbackOnCommitFailure = false;

	@Override
	public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
		TransactionDefinition def = (definition != null ? definition : TransactionDefinition.withDefaults());

		// 获得事务对象
		Object transaction = doGetTransaction();

		if (isExistingTransaction(transaction)) {// 检查是否已有现有事务
			return handleExistingTransaction(def, transaction);
		}

		if (def.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) { // 检查连接超时时间
			throw new InvalidTimeoutException("无效的事务超时时间：" + def.getTimeout());
		}

		// 事务传播
		if (def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
			// 之前已检查是否有现有事务，而逻辑走到此处代表当前没有事务，所以直接报错
			throw new IllegalTransactionStateException("未找到标记为“PROPAGATION_MANDATORY”的事务的现有事务");
		} else if (def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
				def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
				def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
			 // 判断当前线程是否有绑定的事务，若有那么悬挂当前事务，若没有则返回null
			SuspendedResourcesHolder suspendedResources = suspend(null);

			DebugUtils.logFromTransaction(logger, "创建新的事务，by name：[" + def.getName() + "]: " + def);

			try {
				boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
				DefaultTransactionStatus status = newTransactionStatus(def, transaction, true, newSynchronization, suspendedResources);
				doBegin(transaction, def);
				// 根据 TransactionDefinition 配置 TransactionSynchronizationManager 的各个同步状态状态
				prepareSynchronization(status, def);
				return status;
			} catch (RuntimeException | Error ex) {
				resume(null, suspendedResources);
				throw ex;
			}
		} else {
			if (def.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT && logger.isWarnEnabled()) {
				logger.warn("已指定自定义隔离级别，但未启动实际事务；隔离级别将被有效忽略: " + def);
			}
			boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
			return prepareTransactionStatus(def, null, true, newSynchronization, null);
		}
	}

	/**
	 * 恢复给定的事务。首先委托给doResumetemplate方法，然后恢复事务同步
	 * @param transaction
	 * @param resourcesHolder
	 * @throws TransactionException
	 */
	protected final void resume(Object transaction, SuspendedResourcesHolder resourcesHolder) throws TransactionException {
		if (resourcesHolder != null) {
			Object suspendedResources = resourcesHolder.suspendedResources;
			if (suspendedResources != null) {
				doResume(transaction, suspendedResources);
			}
			List<TransactionSynchronization> suspendedSynchronizations = resourcesHolder.suspendedSynchronizations;
			if (suspendedSynchronizations != null) {
				TransactionSynchronizationManager.setActualTransactionActive(resourcesHolder.wasActive);
				TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(resourcesHolder.isolationLevel);
				TransactionSynchronizationManager.setCurrentTransactionReadOnly(resourcesHolder.readOnly);
				TransactionSynchronizationManager.setCurrentTransactionName(resourcesHolder.name);
				doResumeSynchronization(suspendedSynchronizations);
			}
		}
	}

	/**
	 * 恢复当前事务的资源。之后将恢复事务同步
	 * @param transaction
	 * @param suspendedResources
	 * @throws TransactionException
	 */
	protected void doResume(Object transaction, Object suspendedResources) throws TransactionException {
		throw new TransactionSuspensionNotSupportedException("事务管理器 [" + getClass().getName() + "] 不支持暂停事务");
	}

	/**
	 * 挂起所有当前同步并停用当前线程的transactionsynchronization
	 * @return 挂起的TransactionSynchronization对象的列表
	 */
	private List<TransactionSynchronization> doSuspendSynchronization() {
		List<TransactionSynchronization> suspendedSynchronizations =
				TransactionSynchronizationManager.getSynchronizations();
		for (TransactionSynchronization synchronization : suspendedSynchronizations) {
			synchronization.suspend();
		}
		TransactionSynchronizationManager.clearSynchronization();
		return suspendedSynchronizations;
	}

	/**
	 * 重新激活当前线程的事务同步，并恢复所有给定的同步
	 * @param suspendedSynchronizations
	 */
	private void doResumeSynchronization(List<TransactionSynchronization> suspendedSynchronizations) {
		TransactionSynchronizationManager.initSynchronization();
		for (TransactionSynchronization synchronization : suspendedSynchronizations) {
			synchronization.resume();
			TransactionSynchronizationManager.registerSynchronization(synchronization);
		}
	}

	/**
	 * 为给定参数创建新的TransactionStatus，并根据需要初始化事务同步
	 * @param definition
	 * @param transaction
	 * @param newTransaction
	 * @param newSynchronization
	 * @param suspendedResources
	 * @return
	 */
	protected final TransactionStatus prepareTransactionStatus(TransactionDefinition definition, Object transaction, boolean newTransaction,
			boolean newSynchronization, Object suspendedResources) {
		DefaultTransactionStatus status = newTransactionStatus(definition, transaction, newTransaction, newSynchronization, suspendedResources);
		prepareSynchronization(status, definition);
		return status;
	}

	/**
	 * 返回此事务管理器是否应激活绑定到线程的事务
	 * @return
	 */
	public final int getTransactionSynchronization() {
		return this.transactionSynchronization;
	}

	/**
	 * 暂停给定的事务
	 * @param object - 当前事务对象
	 * @return
	 */
	private SuspendedResourcesHolder suspend(Object transaction) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			List<TransactionSynchronization> suspendedSynchronizations = doSuspendSynchronization();
			try {
				Object suspendedResources = null;
				if (transaction != null) {
					suspendedResources = doSuspend(transaction);
				}
				String name = TransactionSynchronizationManager.getCurrentTransactionName();
				TransactionSynchronizationManager.setCurrentTransactionName(null);
				boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
				TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
				Integer isolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
				TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(null);
				boolean wasActive = TransactionSynchronizationManager.isActualTransactionActive();
				TransactionSynchronizationManager.setActualTransactionActive(false);
				return new SuspendedResourcesHolder(suspendedResources, suspendedSynchronizations, name, readOnly, isolationLevel, wasActive);
			} catch (RuntimeException | Error ex) {
				doResumeSynchronization(suspendedSynchronizations);
				throw ex;
			}
		} else if (transaction != null) {
			Object suspendedResources = doSuspend(transaction);
			return new SuspendedResourcesHolder(suspendedResources);
		} else {
			return null;
		}
	}

	/**
	 * 挂起当前事务的资源。事务同步将已挂起
	 * @param transaction
	 * @return
	 * @throws TransactionException
	 */
	protected Object doSuspend(Object transaction) throws TransactionException {
		throw new TransactionSuspensionNotSupportedException("事务管理器 [" + getClass().getName() + "] 不支持事务暂停");
	}

	/**
	 * 为给定参数创建TransactionStatus实例
	 * @param definition - 事务参数定义对象
	 * @param transaction - doGetTransaction返回的事务对象
	 * @param newTransaction
	 * @param newSynchronization
	 * @param suspendedResources - 暂停资源对象
	 * @return
	 */
	protected DefaultTransactionStatus newTransactionStatus(TransactionDefinition definition, Object transaction, 
			boolean newTransaction,	boolean newSynchronization, Object suspendedResources) {
		boolean actualNewSynchronization = newSynchronization && !TransactionSynchronizationManager.isSynchronizationActive();
		return new DefaultTransactionStatus(transaction, newTransaction, actualNewSynchronization,definition.isReadOnly(), suspendedResources);
	}

	/**
	 * 为现有事务创建TransactionStatus
	 * @param def - 事务定义对象
	 * @param transaction - doGetTransaction返回的事务对象
	 * @return
	 */
	private TransactionStatus handleExistingTransaction(TransactionDefinition definition, Object transaction) {
		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
			throw new IllegalTransactionStateException("现有事务的事务传播行为被标记为'never'");
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
			DebugUtils.logFromTransaction(logger, "开始暂停当前事务");
			Object suspendedResources = suspend(transaction);
			boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
			return prepareTransactionStatus(definition, null, false, newSynchronization, suspendedResources);
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
			DebugUtils.logFromTransaction(logger, "开始暂停当前事务");
			SuspendedResourcesHolder suspendedResources = suspend(transaction);
			try {
				boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
				DefaultTransactionStatus status = newTransactionStatus(definition, transaction, true, newSynchronization, suspendedResources);
				doBegin(transaction, definition);
				prepareSynchronization(status, definition);
				return status;
			} catch (RuntimeException | Error beginEx) {
				resumeAfterBeginException(transaction, suspendedResources, beginEx);
				throw beginEx;
			}
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
			if (!isNestedTransactionAllowed()) {
				throw new NestedTransactionNotSupportedException("事务管理器默认不允许嵌套事务-指定'nestedTransactionAllowed'属性值为'true'");
			}
			DebugUtils.logFromTransaction(logger, "创建嵌套的事务，by name：[" + definition.getName() + "]" );
			if (useSavepointForNestedTransaction()) {
				DefaultTransactionStatus status = (DefaultTransactionStatus) prepareTransactionStatus(definition, transaction, false, false, null);
				status.createAndHoldSavepoint();
				return status;
			} else {
				boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
				DefaultTransactionStatus status = newTransactionStatus(definition, transaction, true, newSynchronization, null);
				doBegin(transaction, definition);
				prepareSynchronization(status, definition);
				return status;
			}
		}

		DebugUtils.logFromTransaction(logger, "参与现有事务");
		if (isValidateExistingTransaction()) {
			if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
				// 当前事务隔离级别
				Integer currentIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
				if (currentIsolationLevel == null || currentIsolationLevel != definition.getIsolationLevel()) {
					throw new IllegalTransactionStateException("具有定义[" + definition + "] 的参与事务定义]指定与现有事务不兼容的隔离级别。by 当前隔离级别：" 
							+ currentIsolationLevel +"，参与事务的隔离级别：" + definition.getIsolationLevel());
				}
			}
			if (!definition.isReadOnly()) {
				if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
					throw new IllegalTransactionStateException("具有定义的参与事务[" + definition + "] 没有标记为只读，但现有事务是只读");
				}
			}
		}
		boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
		return prepareTransactionStatus(definition, transaction, false, newSynchronization, null);
	}

	@Override
	public void commit(TransactionStatus status) throws TransactionException {
		if (status.isCompleted()) {
			throw new IllegalTransactionStateException("事务已完成-不要在每个事务中多次调用提交或回滚");
		}

		DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
		if (defStatus.isLocalRollbackOnly()) {
			DebugUtils.logFromTransaction(logger, "事务标记为仅回滚，但事务代码请求提交，将执行回滚逻辑");
			processRollback(defStatus, false);
			return;
		}

		if (!shouldCommitOnGlobalRollbackOnly() && defStatus.isGlobalRollbackOnly()) {
			DebugUtils.logFromTransaction(logger, "全局事务标记为仅回滚，但事务代码请求提交，将执行回滚逻辑");
			processRollback(defStatus, true);
			return;
		}

		processCommit(defStatus);
	}

	/**
	 * 处理实际提交。已检查并应用仅回滚标志
	 * @param status - 事务状态对象
	 * @throws TransactionException - 在提交失败的情况下触发此异常
	 */
	private void processCommit(DefaultTransactionStatus status) throws TransactionException {
		try {
			/** 是否在完成之前调用 */
			boolean beforeCompletionInvoked = false;
			try {
				/** 是否不期望回滚 */
				boolean unexpectedRollback = false;
				prepareForCommit(status);
				triggerBeforeCommit(status);
				triggerBeforeCompletion(status);
				beforeCompletionInvoked = true;

				if (status.hasSavepoint()) {
					DebugUtils.logFromTransaction(logger, "释放事务保存点");
					unexpectedRollback = status.isGlobalRollbackOnly();
					status.releaseHeldSavepoint();
				} else if (status.isNewTransaction()) {
					DebugUtils.logFromTransaction(logger, "发起事务提交");
					unexpectedRollback = status.isGlobalRollbackOnly();
					doCommit(status);
				}
				if (unexpectedRollback) {
					throw new UnexpectedRollbackException("事务以静默方式回滚，因为它已标记为仅回滚");
				}
			} catch (UnexpectedRollbackException ex) {
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
				throw ex;
			} catch (TransactionException ex) {
				// 只能由doCommit引起,即在doCommit中赋值
				if (isRollbackOnCommitFailure()) {
					doRollbackOnCommitException(status, ex);
				} else {
					triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
				}
				throw ex;
			} catch (RuntimeException | Error ex) {
				if (!beforeCompletionInvoked) {
					triggerBeforeCompletion(status);
				}
				doRollbackOnCommitException(status, ex);
				throw ex;
			}
			try {
				triggerAfterCommit(status);
			} finally {
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_COMMITTED);
			}
		} finally {
			cleanupAfterCompletion(status);
		}
	}

	/**
	 * 处理实际回滚。已检查完成标志。
	 * @param status - 事务状态对象
	 * @param unexpected
	 */
	private void processRollback(DefaultTransactionStatus status, boolean unexpected) {
		try {
			boolean unexpectedRollback = unexpected;
			try {
				triggerBeforeCompletion(status);
				if (status.hasSavepoint()) {
					DebugUtils.logFromTransaction(logger, "将事务回滚到保存点");
					status.rollbackToHeldSavepoint();
				} else if (status.isNewTransaction()) {
					DebugUtils.logFromTransaction(logger, "启动事务回滚");
					doRollback(status);
				} else {
					if (status.hasTransaction()) {
						if (status.isLocalRollbackOnly()) {
							DebugUtils.logFromTransaction(logger, "参与事务失败-将现有事务标记为仅回滚");
							doSetRollbackOnly(status);
						} else {
							DebugUtils.logFromTransaction(logger, "参与事务失败-让事务发起人决定回滚");
						}
					} else {
						DebugUtils.logFromTransaction(logger, "应回滚事务，但无法回滚-无可用事务");
					}
				}
			} catch (RuntimeException | Error ex) {
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
				throw ex;
			}

			triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);

			if (unexpectedRollback) {
				throw new UnexpectedRollbackException("事务已回滚，因为它已标记为仅回滚");
			}
		} finally {
			cleanupAfterCompletion(status);
		}
	}

	@Override
	public void rollback(TransactionStatus status) throws TransactionException {
		if (status.isCompleted()) {
			throw new IllegalTransactionStateException("事务已完成-不要在每个事务中多次调用提交或回滚");
		}

		DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
		processRollback(defStatus, false);
	}

	/**
	 * 在beforeCommit同步回调发生之前，准备要执行的提交
	 * @param status
	 */
	protected void prepareForCommit(DefaultTransactionStatus status) {}

	/**
	 * 触发 {@code beforeCommit} 回调
	 * @param status
	 */
	protected final void triggerBeforeCommit(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			DebugUtils.logFromTransaction(logger, "触发[beforeCommit(完成事务提交之前)]");
			TransactionSynchronizationUtils.triggerBeforeCommit(status.isReadOnly());
		}
	}

	/**
	 * 触发 {@code afterCommit }回调
	 * @param status
	 */
	private void triggerAfterCommit(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			DebugUtils.logFromAop(logger,"触发[afterCommit(完成提交之后)]");
			TransactionSynchronizationUtils.triggerAfterCommit();
		}
	}

	/**
	 * 触发{@code beforeCompletion }同步
	 * @param status
	 */
	protected final void triggerBeforeCompletion(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			DebugUtils.logFromTransaction(logger, "触发[beforeCompletion(事务完成前)]");
			TransactionSynchronizationUtils.triggerBeforeCompletion();
		}
	}

	/**
	 * 触发 {@code beforeCompletion} 回调
	 * @param status
	 * @param completionStatus
	 */
	private void triggerAfterCompletion(DefaultTransactionStatus status, int completionStatus) {
		if (status.isNewSynchronization()) {
			List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
			TransactionSynchronizationManager.clearSynchronization();
			if (!status.hasTransaction() || status.isNewTransaction()) {
				DebugUtils.logFromTransaction(logger, "触发[afterCompletion(事务完成后)]");
				TransactionSynchronizationUtils.invokeAfterCompletion(completionStatus, synchronizations);
			} else if (!synchronizations.isEmpty()) {
				registerAfterCompletionWithExistingTransaction(status.getTransaction(), synchronizations);
			}
		}
	}
	
	/**
	 * 将给定的事务同步列表注册到现有事务
	 * @param transaction
	 * @throws TransactionException
	 */
	protected void registerAfterCompletionWithExistingTransaction(Object transaction, List<TransactionSynchronization> synchronizations) throws TransactionException {
		DebugUtils.logFromTransaction(logger, "无法向现有事务注册Spring完成后同步-正在立即处理Spring完成后回调，结果状态为'未知'");
		TransactionSynchronizationUtils.invokeAfterCompletion(TransactionSynchronization.STATUS_UNKNOWN, synchronizations);
	}
	
	/**
	 * doCommit触发异常后调用doRollback，正确处理回滚异常
	 * @param status
	 * @param ex
	 * @throws TransactionException
	 */
	private void doRollbackOnCommitException(DefaultTransactionStatus status, Throwable ex) throws TransactionException {
		try {
			if (status.isNewTransaction()) {
				DebugUtils.logFromTransaction(logger, "在提交异常后启动事务回滚，case：" + ex);
				doRollback(status);
			} else if (status.hasTransaction()) {
				DebugUtils.logFromTransaction(logger, "在提交异常后启动事务回滚，case：" + ex);
				doSetRollbackOnly(status);
			}
		} catch (RuntimeException | Error rbex) {
			triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
			throw rbex;
		}
		triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
	}

	/**
	 * 仅设置给定的事务回滚
	 * @param status
	 */
	protected abstract void doSetRollbackOnly(DefaultTransactionStatus status);

	/**
	 * 完成后进行清理，必要时清除同步，并调用doCleanupAfterCompletion
	 * @param status
	 */
	private void cleanupAfterCompletion(DefaultTransactionStatus status) {
		status.setCompleted();
		if (status.isNewSynchronization()) {
			TransactionSynchronizationManager.clear();
		}
		if (status.isNewTransaction()) {
			doCleanupAfterCompletion(status.getTransaction());
		}
		if (status.getSuspendedResources() != null) {
			DebugUtils.logFromTransaction(logger, "内部事务完成后恢复暂停的事务");
			Object transaction = (status.hasTransaction() ? status.getTransaction() : null);
			resume(transaction, (SuspendedResourcesHolder) status.getSuspendedResources());
		}
	}

	/**
	 * 事务完成后清理资源
	 * @param transaction
	 */
	protected void doCleanupAfterCompletion(Object transaction) {}

	/**
	 * 返回 {@code doCommit} 调用失败时是否应该执行doRollback
	 */
	public final boolean isRollbackOnCommitFailure() {
		return this.rollbackOnCommitFailure;
	}

	public void setRollbackOnCommitFailure(boolean rollbackOnCommitFailure) {
		this.rollbackOnCommitFailure = rollbackOnCommitFailure;
	}

	/**
	 * 返回是否在全局方式中被标记为只回滚的事务上调用doCommit
	 * @return
	 */
	protected boolean shouldCommitOnGlobalRollbackOnly() {
		return false;
	}

	/**
	 * 返回是否应该在参与现有事务之前对其进行验证.
	 */
	public final boolean isValidateExistingTransaction() {
		return this.validateExistingTransaction;
	}

	/**
	 * 返回是否允许嵌套事务
	 */
	public final boolean isNestedTransactionAllowed() {
		return this.nestedTransactionAllowed;
	}

	/**
	 * 返回是否为嵌套事务使用保存点。
	 * <p> Default为true，这将导致委托defaulttransactionstatus来创建和保存保存点。
	 * 如果事务对象没有实现SavepointManager接口，则会抛出NestedTransactionNotSupportedException。
	 * 否则，将要求SavepointManager创建一个新的保存点来界定嵌套事务的开始</p>
	 * @return
	 */
	protected boolean useSavepointForNestedTransaction() {
		return true;
	}

	/**
	 * 内部事务开始失败后恢复外部事务
	 */
	private void resumeAfterBeginException(Object transaction, SuspendedResourcesHolder suspendedResources, Throwable beginEx) {
		try {
			resume(transaction, suspendedResources);
		} catch (RuntimeException | Error resumeEx) {
			logger.error("内部事务开始异常被外部事务恢复异常覆盖", beginEx);
			throw resumeEx;
		}
	}

	/**
	 * 检查给定的事务对象是否指示现有事务（即已启动的事务）
	 * @param transaction - doGetTransaction返回的事务对象
	 * @return
	 * @throws TransactionException
	 */
	protected boolean isExistingTransaction(Object transaction) throws TransactionException {
		return false;
	}

	/**
	 * 根据需要初始化事务同步
	 * @param status
	 * @param definition
	 */
	protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
		if (status.isNewSynchronization()) {
			TransactionSynchronizationManager.setActualTransactionActive(status.hasTransaction());
			TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(
					definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT ?	definition.getIsolationLevel() : null);
			TransactionSynchronizationManager.setCurrentTransactionReadOnly(definition.isReadOnly());
			TransactionSynchronizationManager.setCurrentTransactionName(definition.getName());
			TransactionSynchronizationManager.initSynchronization();
		}
	}

	protected abstract void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException;

	/**
	 * 返回当前事务状态的事务对象
	 * @return
	 * @throws TransactionException
	 */
	protected abstract Object doGetTransaction() throws TransactionException;

	/**
	 * 执行给定事务的实际提交
	 * @param status
	 * @throws TransactionException
	 */
	protected abstract void doCommit(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * 执行给定事务的实际回滚
	 * @param status
	 * @throws TransactionException
	 */
	protected abstract void doRollback(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * 暂停资源持有者
	 * @author Azurite-Y
	 *
	 */
	protected static final class SuspendedResourcesHolder {

		private final Object suspendedResources;

		private List<TransactionSynchronization> suspendedSynchronizations;

		private String name;

		private boolean readOnly;

		private Integer isolationLevel;

		private boolean wasActive;

		private SuspendedResourcesHolder(Object suspendedResources) {
			this.suspendedResources = suspendedResources;
		}

		private SuspendedResourcesHolder(Object suspendedResources, List<TransactionSynchronization> suspendedSynchronizations,
				String name, boolean readOnly, Integer isolationLevel, boolean wasActive) {
			this.suspendedResources = suspendedResources;
			this.suspendedSynchronizations = suspendedSynchronizations;
			this.name = name;
			this.readOnly = readOnly;
			this.isolationLevel = isolationLevel;
			this.wasActive = wasActive;
		}

		public List<TransactionSynchronization> getSuspendedSynchronizations() {
			return suspendedSynchronizations;
		}
		public void setSuspendedSynchronizations(List<TransactionSynchronization> suspendedSynchronizations) {
			this.suspendedSynchronizations = suspendedSynchronizations;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public boolean isReadOnly() {
			return readOnly;
		}
		public void setReadOnly(boolean readOnly) {
			this.readOnly = readOnly;
		}
		public Integer getIsolationLevel() {
			return isolationLevel;
		}
		public void setIsolationLevel(Integer isolationLevel) {
			this.isolationLevel = isolationLevel;
		}
		public boolean isWasActive() {
			return wasActive;
		}
		public void setWasActive(boolean wasActive) {
			this.wasActive = wasActive;
		}
		public Object getSuspendedResources() {
			return suspendedResources;
		}
	}

	public void setTransactionSynchronization(int transactionSynchronization) {
		this.transactionSynchronization = transactionSynchronization;
	}

	public void setValidateExistingTransaction(boolean validateExistingTransaction) {
		this.validateExistingTransaction = validateExistingTransaction;
	}

	public void setNestedTransactionAllowed(boolean nestedTransactionAllowed) {
		this.nestedTransactionAllowed = nestedTransactionAllowed;
	}

	public int getDefaultTimeout() {
		return defaultTimeout;
	}
}

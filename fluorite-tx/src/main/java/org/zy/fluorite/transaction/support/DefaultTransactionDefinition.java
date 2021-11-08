package org.zy.fluorite.transaction.support;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.transaction.annotation.Isolation;
import org.zy.fluorite.transaction.annotation.Propagation;
import org.zy.fluorite.transaction.interfaces.TransactionDefinition;

/**
 * @DateTime 2021年9月15日;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class DefaultTransactionDefinition implements TransactionDefinition, Serializable {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected boolean readOnly = false;

	protected String name;
	
	/** 事务的传播行为 */
	protected int propagationBehavior = PROPAGATION_REQUIRED;

	/** 事务的隔离级别 */
	protected int isolationLevel = ISOLATION_DEFAULT;

	/** 事务的超时时间（秒） */
	protected int timeout = TIMEOUT_DEFAULT;
	
	@Override
	public boolean isReadOnly() {
		return this.readOnly;
	}
	@Override
	public String getName() {
		return this.name;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPropagationBehavior() {
		return propagationBehavior;
	}
	public void setPropagationBehavior(Propagation propagation) {
		this.propagationBehavior = propagation.value();
	}
	public int getIsolationLevel() {
		return isolationLevel;
	}
	public void setIsolationLevel(Isolation isolation) {
		this.isolationLevel = isolation.value();
	}
	public int getTimeout() {
		return timeout;
	}
	/**
	 * 设置事务的超时时间(秒)
	 * @param timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}


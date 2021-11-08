package org.zy.fluorite.transaction.support;

import java.util.Date;

import org.zy.fluorite.transaction.exception.TransactionTimedOutException;
import org.zy.fluorite.transaction.interfaces.ResourceHolder;

/**
 * @DateTime 2021年9月15日;
 * @author zy(azurite-Y);
 * @Description 资源持有者的方便基类。特性只支持参与事务的回滚。可以在一定的秒数或毫秒后过期，以确定事务超时
 */
public class ResourceHolderSupport implements ResourceHolder {
	/** 是否与事务保持同步 */
	private boolean synchronizedWithTransaction = false;

	private boolean rollbackOnly = false;

	/** 最后期限 */
	private Date deadline;

	/** 引用计数 */
	private int referenceCount = 0;

	private boolean isVoid = false;


	/**
	 * 将资源标记为是否与事务同步
	 */
	public void setSynchronizedWithTransaction(boolean synchronizedWithTransaction) {
		this.synchronizedWithTransaction = synchronizedWithTransaction;
	}

	/**
	 * 返回资源是否与事务同步
	 */
	public boolean isSynchronizedWithTransaction() {
		return this.synchronizedWithTransaction;
	}

	/**
	 * 设置事务资源未只回滚
	 */
	public void setRollbackOnly() {
		this.rollbackOnly = true;
	}

	/**
	 * 重置此资源事务的只回滚状态
	 */
	public void resetRollbackOnly() {
		this.rollbackOnly = false;
	}

	/**
	 * 返回事务资源的仅回滚状态
	 */
	public boolean isRollbackOnly() {
		return this.rollbackOnly;
	}

	/**
	 * 设置该对象的超时时间，单位为秒
	 */
	public void setTimeoutInSeconds(int seconds) {
		setTimeoutInMillis(seconds * 1000L);
	}

	/**
	 * 设置该对象的超时时间，单位为毫秒
	 */
	public void setTimeoutInMillis(long millis) {
		this.deadline = new Date(System.currentTimeMillis() + millis);
	}

	/**
	 * 返回该对象是否有关联的超时
	 */
	public boolean hasTimeout() {
		return (this.deadline != null);
	}

	/**
	 * 返回该对象的过期截止日期
	 */
	public Date getDeadline() {
		return this.deadline;
	}

	/**
	 * 返回该对象的生存时间(以秒为单位)。例如9.00001仍然是10
	 */
	public int getTimeToLiveInSeconds() {
		double diff = ((double) getTimeToLiveInMillis()) / 1000;
		int secs = (int) Math.ceil(diff);
		checkTransactionTimeout(secs <= 0);
		return secs;
	}

	/**
	 * 返回该对象的生存时间(以毫秒为单位)
	 */
	public long getTimeToLiveInMillis() throws TransactionTimedOutException{
		if (this.deadline == null) {
			throw new IllegalStateException("没有为该资源占有者指定超时");
		}
		long timeToLive = this.deadline.getTime() - System.currentTimeMillis();
		checkTransactionTimeout(timeToLive <= 0);
		return timeToLive;
	}

	/**
	 * 设置事务是否只在到达截止日期时回滚，并抛出TransactionTimedOutException
	 */
	private void checkTransactionTimeout(boolean deadlineReached) throws TransactionTimedOutException {
		if (deadlineReached) {
			setRollbackOnly();
			throw new TransactionTimedOutException("事务超时:截止日期 [ " + this.deadline + "]");
		}
	}

	/**
	 * 增加一个引用计数，因为持有者已经被请求。有人请求它所持有的资源)。
	 */
	public void requested() {
		this.referenceCount++;
	}

	/**
	 * 引用计数减少1，因为持有者已经被释放。有人释放了它所持有的资源)。
	 */
	public void released() {
		this.referenceCount--;
	}

	/**
	 * 返回是否还有对该holder的开放引用
	 */
	public boolean isOpen() {
		return (this.referenceCount > 0);
	}

	/**
	 * 清除此资源持有者的事务状态
	 */
	public void clear() {
		this.synchronizedWithTransaction = false;
		this.rollbackOnly = false;
		this.deadline = null;
	}

	/**
	 * 重置此ResourceHolder- 事务状态和引用计数
	 */
	@Override
	public void reset() {
		clear();
		this.referenceCount = 0;
	}

	@Override
	public void unbound() {
		this.isVoid = true;
	}

	@Override
	public boolean isVoid() {
		return this.isVoid;
	}
}

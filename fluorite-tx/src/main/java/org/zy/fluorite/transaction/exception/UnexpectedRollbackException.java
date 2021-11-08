package org.zy.fluorite.transaction.exception;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 未预期的事务回滚异常
 */
@SuppressWarnings("serial")
public class UnexpectedRollbackException extends TransactionException {
	public UnexpectedRollbackException(String msg) {
		super(msg);
	}

	public UnexpectedRollbackException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

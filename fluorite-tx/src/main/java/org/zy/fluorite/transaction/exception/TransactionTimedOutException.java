package org.zy.fluorite.transaction.exception;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 事务超时异常
 */
@SuppressWarnings("serial")
public class TransactionTimedOutException extends TransactionException {
	public TransactionTimedOutException(String msg) {
		super(msg);
	}

	public TransactionTimedOutException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

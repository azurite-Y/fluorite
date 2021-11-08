package org.zy.fluorite.transaction.exception;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 无法创建事务异常
 */
@SuppressWarnings("serial")
public class CannotCreateTransactionException extends TransactionException {

	public CannotCreateTransactionException(String msg) {
		super(msg);
	}

	public CannotCreateTransactionException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

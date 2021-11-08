package org.zy.fluorite.transaction.exception;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 事务使用异常
 */
@SuppressWarnings("serial")
public class TransactionUsageException extends TransactionException {

	public TransactionUsageException(String msg) {
		super(msg);
	}

	public TransactionUsageException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

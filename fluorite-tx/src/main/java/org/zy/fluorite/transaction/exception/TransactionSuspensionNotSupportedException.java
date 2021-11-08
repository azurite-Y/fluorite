package org.zy.fluorite.transaction.exception;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 事务暂停不支持异常
 */
@SuppressWarnings("serial")
public class TransactionSuspensionNotSupportedException extends TransactionException {

	public TransactionSuspensionNotSupportedException(String msg) {
		super(msg);
	}

	public TransactionSuspensionNotSupportedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

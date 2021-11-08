package org.zy.fluorite.transaction.exception;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 非法的事务状态异常
 */
@SuppressWarnings("serial")
public class IllegalTransactionStateException extends TransactionException {

	/**
	 * 非法的事务状态异常
	 * @param msg
	 */
	public IllegalTransactionStateException(String msg) {
		super(msg);
	}

	public IllegalTransactionStateException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

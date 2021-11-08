package org.zy.fluorite.transaction.exception;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 嵌套事务不支持异常
 */
@SuppressWarnings("serial")
public class NestedTransactionNotSupportedException extends TransactionException {
	/**
	 * 嵌套事务不支持异常
	 * @param msg
	 */
	public NestedTransactionNotSupportedException(String msg) {
		super(msg);
	}

	/**
	 * 嵌套事务不支持异常
	 * @param msg
	 * @param cause
	 */
	public NestedTransactionNotSupportedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

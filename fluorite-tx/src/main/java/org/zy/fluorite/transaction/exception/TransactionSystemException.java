package org.zy.fluorite.transaction.exception;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 遇到一般事务系统错误时抛出的异常，如提交或回滚时
 */
@SuppressWarnings("serial")
public class TransactionSystemException extends TransactionException {

	public TransactionSystemException(String msg) {
		super(msg);
	}

	public TransactionSystemException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

package org.zy.fluorite.transaction.exception;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description 无效的超时时间设置异常
 */
@SuppressWarnings("serial")
public class InvalidTimeoutException extends TransactionException {

	public InvalidTimeoutException(String msg) {
		super(msg);
	}

	public InvalidTimeoutException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

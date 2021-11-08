package org.zy.fluorite.transaction.exception;

import org.zy.fluorite.core.exception.FluoriteRuntimeException;

/**
 * @author: zy;
 * @DateTime: 2020年6月4日 下午3:14:35;
 * @Description jdbc连接获取异常
 */
@SuppressWarnings("serial")
public class CannotGetJdbcConnectionException extends FluoriteRuntimeException {

	public CannotGetJdbcConnectionException(String msg) {
		super(msg);
	}

	public CannotGetJdbcConnectionException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

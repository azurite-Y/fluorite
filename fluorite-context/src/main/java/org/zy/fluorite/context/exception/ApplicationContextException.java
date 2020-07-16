package org.zy.fluorite.context.exception;

import org.zy.fluorite.core.exception.FluoriteRuntimeException;

/**
 * @DateTime 2020年6月17日 下午3:54:15;
 * @author zy(azurite-Y);
 * @Description ApplicationContext触发的异常基类
 */
@SuppressWarnings("serial")
public class ApplicationContextException extends FluoriteRuntimeException {

	public ApplicationContextException(String msg) {
		super(msg);
	}
	public ApplicationContextException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

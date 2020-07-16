package org.zy.fluorite.context.exception;

import org.zy.fluorite.core.exception.FluoriteRuntimeException;

/**
 * @DateTime 2020年6月17日 下午3:25:38;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class NoSuchMessageException extends FluoriteRuntimeException {

	public NoSuchMessageException(String msg) {
		super(msg);
	}
	public NoSuchMessageException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

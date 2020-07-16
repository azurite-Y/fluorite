package org.zy.fluorite.web.server.exception;

import org.zy.fluorite.core.exception.FluoriteRuntimeException;

/**
 * @DateTime 2020年6月19日 上午12:14:04;
 * @author zy(azurite-Y);
 * @Description web服务器引发的异常基类
 */
@SuppressWarnings("serial")
public class WebServerException extends FluoriteRuntimeException {

	public WebServerException(String msg) {
		super(msg);
	}
	public WebServerException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

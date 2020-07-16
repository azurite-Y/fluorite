package org.zy.fluorite.core.exception;

/**
 * @DateTime 2020年6月18日 上午8:46:17;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class ExpressionFormatException extends FluoriteRuntimeException {

	public ExpressionFormatException(String msg) {
		super(msg);
	}
	public ExpressionFormatException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

package org.zy.fluorite.core.exception;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午1:46:02;
 * @Description 类型转换异常
 */
@SuppressWarnings("serial")
public class TypeConvertionException extends FluoriteRuntimeException {
	public TypeConvertionException(String msg) {
		super(msg);
	}
	public TypeConvertionException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

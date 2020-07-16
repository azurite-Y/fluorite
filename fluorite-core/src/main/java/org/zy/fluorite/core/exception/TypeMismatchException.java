package org.zy.fluorite.core.exception;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午1:47:45;
 * @Description 类型不匹配异常
 */
@SuppressWarnings("serial")
public class TypeMismatchException extends TypeConvertionException {
	public TypeMismatchException(String msg) {
		super(msg);
	}
	public TypeMismatchException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

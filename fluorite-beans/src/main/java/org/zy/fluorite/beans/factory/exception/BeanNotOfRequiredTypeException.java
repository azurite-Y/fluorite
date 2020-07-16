package org.zy.fluorite.beans.factory.exception;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月14日 下午5:21:49;
 * @Description 无法解析的依赖项触发的异常
 */
@SuppressWarnings("serial")
public class BeanNotOfRequiredTypeException extends BeanCreationException {

	public BeanNotOfRequiredTypeException(String msg) {
		super(msg);
	}
	public BeanNotOfRequiredTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

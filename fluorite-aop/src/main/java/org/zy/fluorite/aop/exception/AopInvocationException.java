package org.zy.fluorite.aop.exception;

import org.zy.fluorite.core.exception.FluoriteRuntimeException;

/**
 * @DateTime 2020年7月11日 下午2:54:49;
 * @author zy(azurite-Y);
 * @Description aop调用中方法的方法异常基类
 */
@SuppressWarnings("serial")
public class AopInvocationException extends FluoriteRuntimeException{

	public AopInvocationException(String msg) {
		super(msg);
	}
	public AopInvocationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

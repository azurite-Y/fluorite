package org.zy.fluorite.core.exception;

/**
 * @author: zy;
 * @DateTime: 2020年6月4日 下午3:12:29;
 * @Description Fluorite框架实现运行中触发的异常基类
 */
@SuppressWarnings("serial")
public class FluoriteRuntimeException extends RuntimeException {
	public FluoriteRuntimeException(String msg) {
		super(msg);
	}
	
	public FluoriteRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}

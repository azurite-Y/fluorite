package org.zy.fluorite.aop.exception;

import org.zy.fluorite.core.exception.FluoriteRuntimeException;

/**
 * @DateTime 2020年7月4日 下午1:09:18;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class AopConfigException extends FluoriteRuntimeException {

	public AopConfigException(String msg) {
		super(msg);
	}

	
	public AopConfigException(String msg, Throwable cause) {
		super(msg, cause);
		// TODO 自动生成的构造函数存根
	}

}

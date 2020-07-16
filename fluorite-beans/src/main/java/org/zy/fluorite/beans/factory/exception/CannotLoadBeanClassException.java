package org.zy.fluorite.beans.factory.exception;

import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月9日 下午3:26:15;
 * @Description 不能加载包含异常
 */
@SuppressWarnings("serial")
public class CannotLoadBeanClassException extends BeansException {

	public CannotLoadBeanClassException(String msg) {
		super(msg);
	}
	public CannotLoadBeanClassException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

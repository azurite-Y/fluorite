package org.zy.fluorite.beans.factory.exception;

import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午1:14:57;
 * @Description 当BeanFactory尝试从bean定义创建bean时遇到错误时引发异常
 */
@SuppressWarnings("serial")
public class BeanCreationException extends BeansException {

	public BeanCreationException(String msg) {
		super(msg);
	}
	public BeanCreationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

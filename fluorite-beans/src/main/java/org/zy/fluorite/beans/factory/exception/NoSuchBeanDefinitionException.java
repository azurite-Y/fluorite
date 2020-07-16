package org.zy.fluorite.beans.factory.exception;

import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy;
 * @DateTime: 2020年6月4日 下午3:18:02;
 * @Description 未找到指定Bean定义而触发的异常
 */
@SuppressWarnings("serial")
public class NoSuchBeanDefinitionException extends BeansException {

	public NoSuchBeanDefinitionException(String msg) {
		super(msg);
	}
	public NoSuchBeanDefinitionException(String msg, Throwable cause) {
		super(msg, cause);
	}
	public NoSuchBeanDefinitionException(Class<?> requiredType) {
		this("未找到指定Bean定义而触发的异常，by："+requiredType);
	}

}

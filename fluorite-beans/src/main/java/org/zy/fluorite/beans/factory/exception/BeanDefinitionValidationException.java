package org.zy.fluorite.beans.factory.exception;

import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午6:03:45;
 * @Description
 */
@SuppressWarnings("serial")
public class BeanDefinitionValidationException extends BeansException {

	public BeanDefinitionValidationException(String msg) {
		super(msg);
	}
	public BeanDefinitionValidationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

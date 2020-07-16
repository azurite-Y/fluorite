package org.zy.fluorite.beans.factory.exception;

import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午1:28:48;
 * @Description 当BeanFactory遇到无效的BeanDefinition时引发异常定义：例如不完整或矛盾的bean元数据的情况
 */
@SuppressWarnings("serial")
public class BeanDefinitionStoreException extends BeansException {
	public BeanDefinitionStoreException(String msg) {
		super(msg);
	}
	public BeanDefinitionStoreException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

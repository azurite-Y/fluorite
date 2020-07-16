package org.zy.fluorite.context.exception;

import org.zy.fluorite.core.exception.FluoriteRuntimeException;

/**
 * @DateTime 2020年6月20日 下午11:38:39;
 * @author zy(azurite-Y);
 * @Description 解析注解过程中引发的异常，如循环导入
 */
@SuppressWarnings("serial")
public class BeanDefinitionParsingException extends FluoriteRuntimeException {

	public BeanDefinitionParsingException(String msg) {
		super(msg);
	}
	public BeanDefinitionParsingException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

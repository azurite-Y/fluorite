package org.zy.fluorite.core.exception;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月9日 下午2:25:58;
 * @Description
 */
@SuppressWarnings("serial")
public class BeanIsAbstractException extends BeansException {

	public BeanIsAbstractException(String msg) {
		super(msg);
	}
	public BeanIsAbstractException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

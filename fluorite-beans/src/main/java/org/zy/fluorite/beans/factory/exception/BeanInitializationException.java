package org.zy.fluorite.beans.factory.exception;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月9日 下午2:36:40;
 * @Description 
 */
@SuppressWarnings("serial")
public class BeanInitializationException extends BeanCreationException {

	public BeanInitializationException(String msg) {
		super(msg);
	}
	public BeanInitializationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

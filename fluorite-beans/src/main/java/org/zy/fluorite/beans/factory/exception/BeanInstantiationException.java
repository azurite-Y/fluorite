package org.zy.fluorite.beans.factory.exception;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月12日 下午4:48:31;
 * @Description 调用Bean工厂方法后构造器时发生的异常
 */
@SuppressWarnings("serial")
public class BeanInstantiationException extends BeanCreationException {

	public BeanInstantiationException(String msg) {
		super(msg);
	}
	public BeanInstantiationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

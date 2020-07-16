package org.zy.fluorite.beans.factory.exception;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月8日 下午4:26:01;
 * @Description bean对象再此标记为正在创建所触发的异常
 */
@SuppressWarnings("serial")
public class BeanCurrentlyInCreationException extends BeanCreationException {

	public BeanCurrentlyInCreationException(String msg) {
		super(msg);
	}
	public BeanCurrentlyInCreationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

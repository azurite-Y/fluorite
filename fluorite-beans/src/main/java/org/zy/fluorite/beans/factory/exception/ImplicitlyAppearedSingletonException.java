package org.zy.fluorite.beans.factory.exception;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月12日 下午3:41:34;
 * @Description 隐式的出现单例异常，通常发生在通过多种方式注册同一个Bean的情况下。
 * 比如说：多个FactoryBean实现注册同一个bean。
 */
@SuppressWarnings("serial")
public class ImplicitlyAppearedSingletonException extends BeanCreationException {

	public ImplicitlyAppearedSingletonException(String msg) {
		super(msg);
	}
	public ImplicitlyAppearedSingletonException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

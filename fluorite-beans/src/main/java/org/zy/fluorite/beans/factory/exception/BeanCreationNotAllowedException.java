package org.zy.fluorite.beans.factory.exception;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月9日 下午2:36:40;
 * @Description 被标记为销毁状态的Bean无法从BeanFactory实现中获得
 */
@SuppressWarnings("serial")
public class BeanCreationNotAllowedException extends BeanCreationException {

	public BeanCreationNotAllowedException(String msg) {
		super(msg);
	}
	public BeanCreationNotAllowedException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

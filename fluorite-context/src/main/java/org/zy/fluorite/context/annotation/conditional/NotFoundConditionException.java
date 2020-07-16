package org.zy.fluorite.context.annotation.conditional;

import org.zy.fluorite.core.exception.FluoriteRuntimeException;

/**
 * @DateTime 2020年6月30日 下午2:37:35;
 * @author zy(azurite-Y);
 * @Description 因为无法找到对应的条件注解解析方法而触发的异常
 */
@SuppressWarnings("serial")
public class NotFoundConditionException extends FluoriteRuntimeException {

	public NotFoundConditionException(String msg) {
		super(msg);
	}
	public NotFoundConditionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

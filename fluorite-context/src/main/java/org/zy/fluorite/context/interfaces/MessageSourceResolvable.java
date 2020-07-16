package org.zy.fluorite.context.interfaces;

/**
 * @DateTime 2020年6月17日 下午3:27:25;
 * @author zy(azurite-Y);
 * @Description 适用于MessageSource中消息解析的对象的接口。
 */
@FunctionalInterface
public interface MessageSourceResolvable {
	/**
	 * 按尝试的顺序返回用于解析此消息的代码。因此，最后一个代码将是默认代码。
	 */
	String[] getCodes();

	/**
	 * 返回用于解析此消息的参数数组。
	 * 默认实现只返回空值。
	 */
	default Object[] getArguments() {
		return null;
	}

	/**
	 * 返回用于解析此消息的默认消息，默认返回null
	 */
	default String getDefaultMessage() {
		return null;
	}
}

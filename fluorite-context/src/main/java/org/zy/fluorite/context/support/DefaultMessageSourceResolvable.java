package org.zy.fluorite.context.support;

import java.io.Serializable;
import java.util.Arrays;

import org.zy.fluorite.context.interfaces.MessageSourceResolvable;

/**
 * @DateTime 2020年6月17日 下午3:30:25;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class DefaultMessageSourceResolvable implements MessageSourceResolvable, Serializable {
	private final String[] codes;

	private final Object[] arguments;

	private final String defaultMessage;

	public DefaultMessageSourceResolvable(String code) {
		this(new String[] { code }, null, null);
	}
	public DefaultMessageSourceResolvable(String[] codes) {
		this(codes, null, null);
	}
	public DefaultMessageSourceResolvable(String[] codes, String defaultMessage) {
		this(codes, null, defaultMessage);
	}
	public DefaultMessageSourceResolvable(String[] codes, Object[] arguments) {
		this(codes, arguments, null);
	}
	public DefaultMessageSourceResolvable(String[] codes, Object[] arguments, String defaultMessage) {
		this.codes = codes;
		this.arguments = arguments;
		this.defaultMessage = defaultMessage;
	}
	public DefaultMessageSourceResolvable(MessageSourceResolvable resolvable) {
		this(resolvable.getCodes(), resolvable.getArguments(), resolvable.getDefaultMessage());
	}

	public String getCode() {
		return (this.codes != null && this.codes.length > 0 ? this.codes[this.codes.length - 1] : null);
	}

	@Override
	public String[] getCodes() {
		return this.codes;
	}

	@Override
	public Object[] getArguments() {
		return this.arguments;
	}

	@Override
	public String getDefaultMessage() {
		return this.defaultMessage;
	}

	/**
	 * 指示是否需要呈现指定的默认消息以替换占位符
	 */
	public boolean shouldRenderDefaultMessage() {
		return true;
	}

	@Override
	public String toString() {
		return "DefaultMessageSourceResolvable [codes=" + Arrays.toString(codes) + ", arguments="
				+ Arrays.toString(arguments) + ", defaultMessage=" + defaultMessage + "]";
	}
}

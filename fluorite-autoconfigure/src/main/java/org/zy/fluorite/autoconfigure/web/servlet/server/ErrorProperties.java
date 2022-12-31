package org.zy.fluorite.autoconfigure.web.servlet.server;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description web错误处理的配置属性
 */
public class ErrorProperties {
	/** 错误控制器路径 */
	private String path = "/error";

	/** 包括“exception”属性 */
	private boolean includeException;

	/** 何时包含“trace”属性 */
	private IncludeStacktrace includeStacktrace = IncludeStacktrace.NEVER;

	/** 何时包含“消息”属性 */
	private IncludeAttribute includeMessage = IncludeAttribute.NEVER;

	/** 何时包含“错误”属性 */
	private IncludeAttribute includeBindingErrors = IncludeAttribute.NEVER;

	private final Whitelabel whitelabel = new Whitelabel();

	public String getPath() {
		return this.path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public boolean isIncludeException() {
		return this.includeException;
	}
	public void setIncludeException(boolean includeException) {
		this.includeException = includeException;
	}
	public IncludeStacktrace getIncludeStacktrace() {
		return this.includeStacktrace;
	}
	public void setIncludeStacktrace(IncludeStacktrace includeStacktrace) {
		this.includeStacktrace = includeStacktrace;
	}
	public IncludeAttribute getIncludeMessage() {
		return this.includeMessage;
	}
	public void setIncludeMessage(IncludeAttribute includeMessage) {
		this.includeMessage = includeMessage;
	}
	public IncludeAttribute getIncludeBindingErrors() {
		return this.includeBindingErrors;
	}
	public void setIncludeBindingErrors(IncludeAttribute includeBindingErrors) {
		this.includeBindingErrors = includeBindingErrors;
	}
	public Whitelabel getWhitelabel() {
		return this.whitelabel;
	}

	/**
	 * 包括堆栈跟踪属性选项
	 */
	public enum IncludeStacktrace {
		/** 切勿添加堆栈跟踪信息 */
		NEVER,

		/** 始终添加堆栈跟踪信息 */
		ALWAYS,

		/** 当适当的请求参数为“true”时添加错误属性 */
		ON_PARAM,
	}

	/** 包括错误属性选项 */
	public enum IncludeAttribute {
		/** 从不添加错误属性 */
		NEVER,

		/** 始终添加错误属性 */
		ALWAYS,

		/** 当适当的请求参数为“true”时，添加error属性 */
		ON_PARAM
	}

	public static class Whitelabel {
		/** 如果发生服务器错误，是否启用浏览器中显示的默认错误页面 */
		private boolean enabled = true;

		public boolean isEnabled() {
			return this.enabled;
		}
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
}

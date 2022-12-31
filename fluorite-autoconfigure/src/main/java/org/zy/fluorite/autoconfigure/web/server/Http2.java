package org.zy.fluorite.autoconfigure.web.server;

/**
 * @dateTime 2021年12月23日;
 * @author zy(azurite-Y);
 * @description HTTP/2配置的简单独立于服务器的抽象
 */
public class Http2 {
	private boolean enabled = false;

	/**
	 * 返回是否启用HTTP/2支持（如果当前环境支持）。
	 * @return 为{@code true}则启用HTTP/2支持
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}

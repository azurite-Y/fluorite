package org.zy.fluorite.autoconfigure.web.server;

/**
 * @dateTime 2021年12月23日;
 * @author zy(azurite-Y);
 * @description 简单的独立于服务器的压缩配置抽象。
 */
public class Compression {
	private boolean enabled = false;

	private String[] mimeTypes = new String[] { "text/html", "text/xml", "text/plain", "text/css", "text/javascript", "application/javascript", "application/json", "application/xml" };

	/** 排除的UserAgent */
	private String[] excludedUserAgents = null;

	// 2K
	private int minResponseSize = 2048;

	/**
	 * 返回是否启用响应压缩
	 * @return {@code true} - 如果启用了响应压缩
	 */
	public boolean getEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * 返回应压缩的MIME类型。
	 * @return 应该压缩的MIME类型
	 */
	public String[] getMimeTypes() {
		return this.mimeTypes;
	}

	public void setMimeTypes(String[] mimeTypes) {
		this.mimeTypes = mimeTypes;
	}

	public String[] getExcludedUserAgents() {
		return this.excludedUserAgents;
	}

	public void setExcludedUserAgents(String[] excludedUserAgents) {
		this.excludedUserAgents = excludedUserAgents;
	}

	/**
	 * 返回执行压缩所需的最小“内容长度”值
	 * 
	 * @return 压缩所需的最小内容大小（字节）
	 */
	public int getMinResponseSize() {
		return this.minResponseSize;
	}

	public void setMinResponseSize(int minSize) {
		this.minResponseSize = minSize;
	}
}

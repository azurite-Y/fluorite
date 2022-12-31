package org.zy.fluorite.autoconfigure.web.servlet;

import org.zy.fluorite.core.annotation.ConfigurationProperties;

/**
 * @dateTime 2022年12月30日;
 * @author zy(azurite-Y);
 * @description
 */
@ConfigurationProperties(prefix = "fluorite.servlet.multipart")
public class MultipartProperties {

	/**
	 * 是否支持 multipart uploads.
	 */
	private boolean enabled = true;

	/**
	 * 上传文件的中间位置
	 */
	private String location;

	/**
	 * 最大文件尺寸, 默认值20M(20 * 1024 * 1024)
	 */
	private long maxFileSize = 20971520;

	/**
	 * 最大请求大小, 默认值10M(10 * 1024 * 1024 bit)
	 */
	private long maxRequestSize = 10485760;

	/**
	 * 文件写入磁盘的阈值, 默认值2M(2 * 1024 * 1024 bit)
	 */
	private int fileSizeThreshold = 209152;

	/**
	 * 是否在文件或参数访问时惰性地解析 multipart 请求
	 */
	private boolean resolveLazily = false;

	
	
	public boolean getEnabled() {
		return this.enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getLocation() {
		return this.location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public long getMaxFileSize() {
		return this.maxFileSize;
	}
	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}
	public long getMaxRequestSize() {
		return this.maxRequestSize;
	}
	public void setMaxRequestSize(long maxRequestSize) {
		this.maxRequestSize = maxRequestSize;
	}
	public int getFileSizeThreshold() {
		return this.fileSizeThreshold;
	}
	public void setFileSizeThreshold(int fileSizeThreshold) {
		this.fileSizeThreshold = fileSizeThreshold;
	}
	public boolean isResolveLazily() {
		return this.resolveLazily;
	}
	public void setResolveLazily(boolean resolveLazily) {
		this.resolveLazily = resolveLazily;
	}
}

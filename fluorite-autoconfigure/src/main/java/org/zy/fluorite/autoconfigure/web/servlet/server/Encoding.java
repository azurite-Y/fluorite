package org.zy.fluorite.autoconfigure.web.servlet.server;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description 服务器HTTP编码的配置属性
 */
public class Encoding {
	/** Servlet应用程序的默认HTTP编码 */
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	/** HTTP请求和响应的字符集。如果未明确设置，则添加到“Content-Type”标题 */
	private Charset charset = DEFAULT_CHARSET;

	/** 是否强制HTTP请求和响应的编码为配置的字符集 */
	private Boolean force;

	/** 是否在HTTP请求时强制使用配置的字符集编码。没有指定force时默认为true */
	private Boolean forceRequest;

	/** 是否在HTTP响应上强制编码为配置的字符集 */
	private Boolean forceResponse;

	/** 要在其中编码映射的区域设置 */
	private Map<Locale, Charset> mapping;

	public Charset getCharset() {
		return this.charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public boolean isForce() {
		return Boolean.TRUE.equals(this.force);
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public boolean isForceRequest() {
		return Boolean.TRUE.equals(this.forceRequest);
	}

	public void setForceRequest(boolean forceRequest) {
		this.forceRequest = forceRequest;
	}

	public boolean isForceResponse() {
		return Boolean.TRUE.equals(this.forceResponse);
	}

	public void setForceResponse(boolean forceResponse) {
		this.forceResponse = forceResponse;
	}

	public Map<Locale, Charset> getMapping() {
		return this.mapping;
	}

	public void setMapping(Map<Locale, Charset> mapping) {
		this.mapping = mapping;
	}

	public boolean shouldForce(Type type) {
		Boolean force = (type != Type.REQUEST) ? this.forceResponse : this.forceRequest;
		if (force == null) {
			force = this.force;
		}
		if (force == null) {
			force = (type == Type.REQUEST);
		}
		return force;
	}

	/**
	 * Type of HTTP message to consider for encoding configuration.
	 */
	public enum Type {

		/**
		 * HTTP request message.
		 */
		REQUEST,
		/**
		 * HTTP response message.
		 */
		RESPONSE

	}
}

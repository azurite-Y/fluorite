package org.zy.fluorite.autoconfigure.web.servlet.server;

import java.io.File;
import java.time.Duration;
import java.util.Set;

/**
 * @dateTime 2021年12月24日;
 * @author zy(azurite-Y);
 * @description session 属性
 */
public class Session {
	private Duration timeout = Duration.ofMinutes(30);

	private Set<Session.SessionTrackingMode> trackingModes;

	/** 是否持久化 */
	private boolean persistent;

	/**
	 * 用于存储会话数据的目录.
	 */
	private File storeDir;

	private final Cookie cookie = new Cookie();

	private final SessionStoreDirectory sessionStoreDirectory = new SessionStoreDirectory();

	
	public Cookie getCookie() {
		return this.cookie;
	}

	public Duration getTimeout() {
		return this.timeout;
	}

	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}

	/**
	 * 返回 {@link SessionTrackingMode 会话跟踪模式}.
	 * @return 会话跟踪模式
	 */
	public Set<Session.SessionTrackingMode> getTrackingModes() {
		return this.trackingModes;
	}

	public void setTrackingModes(Set<Session.SessionTrackingMode> trackingModes) {
		this.trackingModes = trackingModes;
	}

	/**
	 * 返回是否在重新启动之间保留会话数据.
	 * @return 如果为{@code true}，则在重新启动之间保留会话数据.
	 */
	public boolean isPersistent() {
		return this.persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	/**
	 * 返回用于存储会话数据的目录.
	 */
	public File getStoreDir() {
		return this.storeDir;
	}

	public void setStoreDir(File storeDir) {
		this.sessionStoreDirectory.setDirectory(storeDir);
		this.storeDir = storeDir;
	}

	public SessionStoreDirectory getSessionStoreDirectory() {
		return this.sessionStoreDirectory;
	}
	

	/**
	 * Cookie属性.
	 */
	public static class Cookie {

		private String name;

		private String domain;

		private String path;

		private String comment;

		private Boolean httpOnly;

		private Boolean secure;

		private Duration maxAge;

		/**
		 * 返回会话cookie名称.
		 */
		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		/**
		 * 返回会话cookie的域.
		 */
		public String getDomain() {
			return this.domain;
		}

		public void setDomain(String domain) {
			this.domain = domain;
		}

		/**
		 * 返回会话cookie的路径.
		 */
		public String getPath() {
			return this.path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		/**
		 * 返回会话cookie的注释
		 */
		public String getComment() {
			return this.comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		/**
		 * 返回是否对会话cookie使用“HttpOnly”cookie.
		 * @return {@code true}则 将"HttpOnly"应用于会话Cookie.
		 */
		public Boolean getHttpOnly() {
			return this.httpOnly;
		}

		public void setHttpOnly(Boolean httpOnly) {
			this.httpOnly = httpOnly;
		}

		/**
		 * 返回是否始终将会话cookie标记为安全的.
		 * @return 如果为{@code true}，则即使启动相应会话的请求使用纯HTTP，也会将会话cookie标记为安全的
		 */
		public Boolean getSecure() {
			return this.secure;
		}

		public void setSecure(Boolean secure) {
			this.secure = secure;
		}

		/**
		 * 返回会话cookie的最大使用期限.
		 * @return 会话cookie的最大使用期限
		 */
		public Duration getMaxAge() {
			return this.maxAge;
		}

		public void setMaxAge(Duration maxAge) {
			this.maxAge = maxAge;
		}

	}

	/**
	 * 可用的会话跟踪模式 (mirrors {@link javax.servlet.SessionTrackingMode}.
	 */
	public enum SessionTrackingMode {

		/**
		 * 发送cookie以响应客户端的第一个请求.
		 */
		COOKIE,

		/**
		 * 重写URL以附加会话ID.
		 */
		URL,

		/**
		 * 使用SSL内置机制跟踪会话.
		 */
		SSL

	}
}

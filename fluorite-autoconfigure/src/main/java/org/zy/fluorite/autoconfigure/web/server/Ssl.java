package org.zy.fluorite.autoconfigure.web.server;

/**
 * @dateTime 2021年12月23日;
 * @author zy(azurite-Y);
 * @description SSL配置的简单服务器独立抽象
 */
public class Ssl {
	private boolean enabled = true;

	private ClientAuth clientAuth;

	private String[] ciphers;

	private String[] enabledProtocols;

	private String keyAlias;

	private String keyPassword;

	private String keyStore;

	private String keyStorePassword;

	private String keyStoreType;

	private String keyStoreProvider;

	private String trustStore;

	private String trustStorePassword;

	private String trustStoreType;

	private String trustStoreProvider;

	private String protocol = "TLS";

	/**
	 * 返回是否启用SSL支持
	 * @return 是否启用SSL支持
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * 返回是否不需要（"none"）、需要（"want"）或需要（"need"）客户端身份验证。需要信任存储。
	 * @return 要使用的{@link ClientAuth}
	 */
	public ClientAuth getClientAuth() {
		return this.clientAuth;
	}

	public void setClientAuth(ClientAuth clientAuth) {
		this.clientAuth = clientAuth;
	}

	/**
	 * 返回支持的SSL密码。
	 * @return 受支持的SSL密码
	 */
	public String[] getCiphers() {
		return this.ciphers;
	}

	public void setCiphers(String[] ciphers) {
		this.ciphers = ciphers;
	}

	/**
	 * 返回启用的SSL协议
	 * @return 启用的SSL协议
	 */
	public String[] getEnabledProtocols() {
		return this.enabledProtocols;
	}

	public void setEnabledProtocols(String[] enabledProtocols) {
		this.enabledProtocols = enabledProtocols;
	}

	/**
	 * 返回标识密钥存储中密钥的别名。
	 */
	public String getKeyAlias() {
		return this.keyAlias;
	}

	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}

	/**
	 * 返回用于访问密钥存储中密钥的密码。
	 */
	public String getKeyPassword() {
		return this.keyPassword;
	}

	public void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword;
	}

	/**
	 * 返回保存SSL证书（通常是jksfile）的密钥存储的路径.
	 * @return 密钥存储的路径
	 */
	public String getKeyStore() {
		return this.keyStore;
	}

	public void setKeyStore(String keyStore) {
		this.keyStore = keyStore;
	}

	/**
	 * 返回用于访问密钥存储的密码.
	 */
	public String getKeyStorePassword() {
		return this.keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	/**
	 * 返回密钥存储的类型.
	 */
	public String getKeyStoreType() {
		return this.keyStoreType;
	}

	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}

	/**
	 * 返回密钥存储的提供程序.
	 * @return 密钥存储提供程序
	 */
	public String getKeyStoreProvider() {
		return this.keyStoreProvider;
	}

	public void setKeyStoreProvider(String keyStoreProvider) {
		this.keyStoreProvider = keyStoreProvider;
	}

	/**
	 * 返回保存SSL证书的信任存储.
	 * @return 信任存储区
	 */
	public String getTrustStore() {
		return this.trustStore;
	}

	public void setTrustStore(String trustStore) {
		this.trustStore = trustStore;
	}

	/**
	 * 返回用于访问信任存储区的密码
	 * @return 信任存储区密码
	 */
	public String getTrustStorePassword() {
		return this.trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	/**
	 * 返回信任存储库的类型.
	 * @return 信任存储库类型
	 */
	public String getTrustStoreType() {
		return this.trustStoreType;
	}

	public void setTrustStoreType(String trustStoreType) {
		this.trustStoreType = trustStoreType;
	}

	/**
	 * 返回信任存储库的提供程序
	 * @return 信任存储库提供者
	 */
	public String getTrustStoreProvider() {
		return this.trustStoreProvider;
	}

	public void setTrustStoreProvider(String trustStoreProvider) {
		this.trustStoreProvider = trustStoreProvider;
	}

	/**
	 * 返回要使用的SSL协议
	 * @return SSL协议
	 */
	public String getProtocol() {
		return this.protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * 客户端身份验证类型
	 */
	public enum ClientAuth {
		/**
		 * 不需要客户端身份验证.
		 */
		NONE,

		/**
		 * 客户端身份验证是需要的，但不是强制性的.
		 */
		WANT,

		/**
		 * 客户端身份验证是必需的.
		 */
		NEED
	}
}

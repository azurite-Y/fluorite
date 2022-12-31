package org.zy.fluorite.autoconfigure.web.server.interfaces;

import java.security.KeyStore;

/**
 * @dateTime 2021年12月23日;
 * @author zy(azurite-Y);
 * @description 接口，以提供Web服务器使用的SSL密钥存储。当无法使用基于文件的密钥存储时可以使用。
 */
public interface SslStoreProvider {
	/**
	 * 返回应该使用的密钥存储。
	 * @return 要使用的密钥存储
	 * @throws 加载时异常错误
	 */
	KeyStore getKeyStore() throws Exception;

	/**
	 * 返回应使用的信任存储。
	 * @return 要使用的信任存储
	 * @throws 加载时异常错误
	 */
	KeyStore getTrustStore() throws Exception;
}

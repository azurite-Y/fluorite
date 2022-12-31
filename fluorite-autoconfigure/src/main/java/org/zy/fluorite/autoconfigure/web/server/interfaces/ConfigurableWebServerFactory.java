package org.zy.fluorite.autoconfigure.web.server.interfaces;

import java.net.InetAddress;
import java.util.Set;

import org.zy.fluorite.autoconfigure.web.server.Compression;
import org.zy.fluorite.autoconfigure.web.server.ErrorPage;
import org.zy.fluorite.autoconfigure.web.server.Http2;
import org.zy.fluorite.autoconfigure.web.server.Ssl;

/**
 * @dateTime 2021年12月23日;
 * @author zy(azurite-Y);
 * @description 配置WebServerFactory
 */
public interface ConfigurableWebServerFactory extends WebServerFactory, ErrorPageRegistry {
	/**
	 * 设置web服务器应该监听的端口。如果没有指定，端口'8080'将被使用。使用端口 -1 禁用自动启动(即启动web应用程序上下文，但不让它监听任何端口)。
	 * @param port - 要设置的端口
	 */
	void setPort(int port);

	/**
	 * 设置服务器应绑定到的特定网络地址。
	 * @param address - 要设置的地址 (默认为{@code null})
	 */
	void setAddress(InetAddress address);

	/**
	 * 设置处理异常时将使用的错误页
	 */
	void setErrorPages(Set<? extends ErrorPage> errorPages);

	/**
	 * 设置将应用于服务器默认连接器的SSL配置。
	 */
	void setSsl(Ssl ssl);

	/**
	 * 设置将用于获取SSL存储的提供程序。
	 */
	void setSslStoreProvider(SslStoreProvider sslStoreProvider);

	/**
	 * 设置将应用于服务器的HTTP/2配置。
	 */
	void setHttp2(Http2 http2);

	/**
	 * 设置将应用于服务器默认连接器的压缩配置
	 */
	void setCompression(Compression compression);

	/**
	 * 设置服务器头的值
	 */
	void setServerHeader(String serverHeader);
	
	/**
	 * 设置将应用于服务器的关机配置
	 * 
	 * @param shutdown - 关机配置
	 */
	default void setShutdown(Shutdown shutdown) {}
}

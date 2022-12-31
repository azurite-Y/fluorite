package org.zy.fluorite.autoconfigure.web.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.zy.fluorite.autoconfigure.web.server.interfaces.ConfigurableWebServerFactory;
import org.zy.fluorite.autoconfigure.web.server.interfaces.Shutdown;
import org.zy.fluorite.autoconfigure.web.server.interfaces.SslStoreProvider;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.web.server.exception.WebServerException;

/**
 * @dateTime 2021年12月23日;
 * @author zy(azurite-Y);
 * @description 用于ConfigurableWebServerFactory实现的抽象基类
 */
public abstract class AbstractConfigurableWebServerFactory implements ConfigurableWebServerFactory {
	private int port = 8080;

	private InetAddress address;

	private Set<ErrorPage> errorPages = new LinkedHashSet<>();

	private Ssl ssl;

	private SslStoreProvider sslStoreProvider;

	private Http2 http2;

	private Compression compression;

	private String serverHeader;

	private Shutdown shutdown = Shutdown.IMMEDIATE;
	
	/**
	 * 创建一个新的 {@link AbstractConfigurableWebServerFactory} 实例.
	 */
	public AbstractConfigurableWebServerFactory() {
	}

	/**
	 * 使用指定的端口创建一个新的 {@link AbstractConfigurableWebServerFactory}实例.
	 * @param port - web服务器的端口号
	 */
	public AbstractConfigurableWebServerFactory(int port) {
		this.port = port;
	}

	/**
	 * web服务器监听的端口.
	 */
	public int getPort() {
		return this.port;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * 返回web服务器绑定的地址。
	 */
	public InetAddress getAddress() {
		return this.address;
	}

	@Override
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	/**
	 * 返回一个可变的ErrorPages集合，用于处理异常。
	 */
	public Set<ErrorPage> getErrorPages() {
		return this.errorPages;
	}

	@Override
	public void setErrorPages(Set<? extends ErrorPage> errorPages) {
		Assert.notNull(errorPages, "ErrorPages不能为null");
		this.errorPages = new LinkedHashSet<>(errorPages);
	}

	@Override
	public void addErrorPages(ErrorPage... errorPages) {
		Assert.notNull(errorPages, "ErrorPages不能为null");
		this.errorPages.addAll(Arrays.asList(errorPages));
	}

	public Ssl getSsl() {
		return this.ssl;
	}

	@Override
	public void setSsl(Ssl ssl) {
		this.ssl = ssl;
	}

	public SslStoreProvider getSslStoreProvider() {
		return this.sslStoreProvider;
	}

	@Override
	public void setSslStoreProvider(SslStoreProvider sslStoreProvider) {
		this.sslStoreProvider = sslStoreProvider;
	}

	public Http2 getHttp2() {
		return this.http2;
	}

	@Override
	public void setHttp2(Http2 http2) {
		this.http2 = http2;
	}

	public Compression getCompression() {
		return this.compression;
	}

	@Override
	public void setCompression(Compression compression) {
		this.compression = compression;
	}

	public String getServerHeader() {
		return this.serverHeader;
	}

	@Override
	public void setServerHeader(String serverHeader) {
		this.serverHeader = serverHeader;
	}
	
	@Override
	public void setShutdown(Shutdown shutdown) {
		this.shutdown = shutdown;
	}

	/**
	 * 返回将应用于服务器的关闭配置
	 * 
	 * @return 关闭配置
	 */
	public Shutdown getShutdown() {
		return this.shutdown;
	}

	/**
	 * 返回指定web服务器的绝对临时目录
	 * @param prefix - 服务器名称
	 * @return 给定服务器的临时目录.
	 */
	protected final File createTempDir(String prefix) {
		try {
			File tempDir = File.createTempFile(prefix + ".", "." + getPort());
			tempDir.delete();
			tempDir.mkdir();
			tempDir.deleteOnExit();
			return tempDir;
		}
		catch (IOException ex) {
			throw new WebServerException("无法创建tempDir. Java.io.tmpdir被设置为" + System.getProperty("java.io.tmpdir"), ex);
		}
	}
}

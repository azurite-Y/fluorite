package org.zy.fluorite.autoconfigure.web.server.interfaces;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;

import org.zy.fluorite.autoconfigure.web.server.MimeMappings;
import org.zy.fluorite.autoconfigure.web.servlet.interfaces.ServletContextInitializer;
import org.zy.fluorite.autoconfigure.web.servlet.interfaces.ServletWebServerFactory;
import org.zy.fluorite.autoconfigure.web.servlet.server.Session;

/**
 * @dateTime 2021年12月23日;
 * @author zy(azurite-Y);
 * @description 一个可配置的ServletWebServerFactory
 */
public interface ConfigurableServletWebServerFactory extends ConfigurableWebServerFactory, ServletWebServerFactory {

	/**
	 * 设置web服务器的上下文路径。上下文应该以"/"字符开头，而不是以"/"字符结尾。默认的上下文路径可以使用空字符串指定。
	 * @param contextPath - 要设置的上下文路径
	 */
	void setContextPath(String contextPath);

	/**
	 * 设置部署在web服务器中的应用程序的显示名称.
	 * @param displayName - 要设置的显示名称
	 */
	void setDisplayName(String displayName);

	/**
	 * 设置将应用于容器的HTTP会话支持的配置.
	 * @param session - 会话配置
	 */
	void setSession(Session session);

	/**
	 * 设置是否应该注册DefaultServlet。默认值为true，以便服务于来自文档根目录的文件
	 * @param registerDefaultServlet - 如果应该注册默认的servlet
	 */
	void setRegisterDefaultServlet(boolean registerDefaultServlet);

	/**
	 * 设置mime类型映射
	 * @param mimeMappings mime类型映射(默认为 {@link MimeMappings.DEFAULT})
	 */
	void setMimeMappings(MimeMappings mimeMappings);

	/**
	 * 设置将被web上下文用于servstatic文件的文档根目录
	 * @param documentRoot 文档根目录或者{@code null}(如果不需要)
	 */
	void setDocumentRoot(File documentRoot);

	/**
	 * 设置 {@link ServletContextInitializer}，它应该应用于 {@link ServletWebServerFactory.getWebServer(ServletContextInitializer)} 参数之外。
	 * 此方法将替换以前设置或添加的任何初始化式。
	 * @param initializers 要设置的 ServletContextInitializer
	 * @see #addInitializers
	 */
	void setInitializers(List<? extends ServletContextInitializer> initializers);

	/**
	 * 添加 {@link ServletContextInitializer} 到那些除了 {@link ServletWebServerFactory#getWebServer(ServletContextInitializer...)} 之外应该应用的参数中
	 * @see #setInitializers
	 */
	void addInitializers(ServletContextInitializer... initializers);

	/**
	 * 将区域设置为字符集映射
	 * @param localeCharsetMappings- 区域设置到字符集的映射
	 */
	void setLocaleCharsetMappings(Map<Locale, Charset> localeCharsetMappings);

	/**
	 * 设置应用于容器 {@link ServletContext}的初始化参数
	 * @param initParameters - 初始化参数
	 */
	void setInitParameters(Map<String, String> initParameters);
}

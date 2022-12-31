package org.zy.fluorite.autoconfigure.web.servlet.interfaces;

import org.zy.fluorite.web.server.interfaces.WebServer;

/**
 * @dateTime 2021年12月24日;
 * @author zy(azurite-Y);
 * @description 工厂接口，可以用来创建一个{@link WebServer}.
 */
@FunctionalInterface
public interface ServletWebServerFactory {
	/**
	 * 获取已完全配置但已暂停的新WebServer实例。
	 * 客户端应该不能连接到返回的服务器，直到WebServer.start()被调用(当ApplicationContext被完全刷新时发生)。
	 * @param initializers {@link ServletContextInitializer ServletContextInitializers} 应该在服务器启动时应用
	 * @return 一个完全配置并启动的{@link WebServer}
	 * @see WebServer#stop()
	 */
	WebServer getWebServer(ServletContextInitializer... initializers);
}

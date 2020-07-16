package org.zy.fluorite.web.context.interfaces;

import org.zy.fluorite.context.interfaces.ApplicationContext;
import org.zy.fluorite.web.server.interfaces.WebServer;

/**
 * @DateTime 2020年6月19日 上午12:08:45;
 * @author zy(azurite-Y);
 * @Description 表示完全配置的web服务器的简单接口（例如Tomcat、Jetty、Netty）。允许启动和停止服务器。
 */
public interface WebServerApplicationContext {
	/**
	 * 返回由上下文创建的Web服务器；如果尚未创建服务器，则返回空值
	 */
	WebServer getWebServer();

	/**
	 * 返回web服务器应用程序上下文的命名空间，如果已设置非命名空间，则返回空。
	 * 用于在同一应用程序中运行多个web服务器时消除歧义（例如在不同端口上运行的管理上下文）
	 */
	String getServerNamespace();
	
	/**
	 * 设置上下文的服务器命名空间
	 * @param serverNamespace
	 */
	void setServerNamespace(String serverNamespace);

	/**
	 * 如果指定的上下文是具有匹配服务器命名空间的WebServerApplicationContext，则返回true。
	 */
	static boolean hasServerNamespace(ApplicationContext context, String serverNamespace) {
		return (context instanceof WebServerApplicationContext)
				&& ((WebServerApplicationContext) context).getServerNamespace().equals(serverNamespace);
	}
}

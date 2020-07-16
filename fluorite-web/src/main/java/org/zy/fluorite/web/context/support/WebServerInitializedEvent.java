package org.zy.fluorite.web.context.support;

import org.zy.fluorite.context.event.ApplicationEvent;
import org.zy.fluorite.web.context.interfaces.WebServerApplicationContext;
import org.zy.fluorite.web.server.interfaces.WebServer;

/**
 * @DateTime 2020年6月19日 上午9:46:37;
 * @author zy(azurite-Y);
 * @Description 在刷新应用程序上下文并准备好Web服务器后要发布的事件。用于获取正在运行的服务器的本地端口
 */
@SuppressWarnings("serial")
public abstract class WebServerInitializedEvent extends ApplicationEvent  {
	protected WebServerInitializedEvent(WebServer webServer) {
		super(webServer);
	}

	/**
	 * 访问Web服务器
	 */
	public WebServer getWebServer() {
		return getSource();
	}

	public abstract WebServerApplicationContext getApplicationContext();

	/**
	 * 访问事件的源（Web服务器）
	 */
	@Override
	public WebServer getSource() {
		return (WebServer) super.getSource();
	}
}

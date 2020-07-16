package org.zy.fluorite.boot.web.servlet;

import org.zy.fluorite.web.context.support.WebServerInitializedEvent;
import org.zy.fluorite.web.server.interfaces.WebServer;

/**
 * @DateTime 2020年6月19日 上午9:51:23;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class ServletWebServerInitializedEvent extends WebServerInitializedEvent{
	private final ServletWebServerApplicationContext applicationContext;

	public ServletWebServerInitializedEvent(WebServer webServer, ServletWebServerApplicationContext applicationContext) {
		super(webServer);
		this.applicationContext = applicationContext;
	}

	@Override
	public ServletWebServerApplicationContext getApplicationContext() {
		return this.applicationContext;
	}
}

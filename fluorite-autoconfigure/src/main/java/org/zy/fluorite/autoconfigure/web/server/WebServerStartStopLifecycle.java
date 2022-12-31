package org.zy.fluorite.autoconfigure.web.server;

import org.zy.fluorite.autoconfigure.web.servlet.ServletWebServerApplicationContext;
import org.zy.fluorite.autoconfigure.web.servlet.ServletWebServerInitializedEvent;
import org.zy.fluorite.context.interfaces.SmartLifecycle;
import org.zy.fluorite.web.server.interfaces.WebServer;

/**
 * @dateTime 2022年12月29日;
 * @author zy(azurite-Y);
 * @description {@link SmartLifecycle} 在 {@link ServletWebServerApplicationContext} 中启动和停止 {@link WebServer}。
 */
public class WebServerStartStopLifecycle implements SmartLifecycle {

	private final ServletWebServerApplicationContext applicationContext;

	private final WebServer webServer;

	private volatile boolean running;

	public WebServerStartStopLifecycle(ServletWebServerApplicationContext applicationContext, WebServer webServer) {
		this.applicationContext = applicationContext;
		this.webServer = webServer;
	}

	@Override
	public void start() {
		this.webServer.start();
		this.running = true;
		this.applicationContext.publishEvent(new ServletWebServerInitializedEvent(this.webServer, this.applicationContext));
	}

	@Override
	public void stop() {
		this.webServer.stop();
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE - 1;
	}

}

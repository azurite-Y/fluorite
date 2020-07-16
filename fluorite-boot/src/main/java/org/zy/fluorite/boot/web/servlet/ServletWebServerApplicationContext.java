package org.zy.fluorite.boot.web.servlet;

import javax.servlet.ServletConfig;

import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.context.exception.ApplicationContextException;
import org.zy.fluorite.context.support.DefaultListableBeanFactory;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.web.context.interfaces.WebServerApplicationContext;
import org.zy.fluorite.web.context.interfaces.auare.ServletContextAware;
import org.zy.fluorite.web.context.support.GenericWebApplicationContext;
import org.zy.fluorite.web.context.support.WebApplicationContextServletContextAwareProcessor;
import org.zy.fluorite.web.server.interfaces.WebServer;

/**
 * @DateTime 2020年6月19日 上午12:04:10;
 * @author zy(azurite-Y);
 * @Description
 */
public class ServletWebServerApplicationContext extends GenericWebApplicationContext
		implements WebServerApplicationContext {

	/**
	 * DispatcherServlet bean名称的常量值。 具有此名称的Servlet
	 * bean被视为“main”Servlet，默认情况下会自动给出“/”的映射。
	 */
	public static final String DISPATCHER_SERVLET_NAME = "dispatcherServlet";

	private volatile WebServer webServer;

	private ServletConfig servletConfig;

	private String serverNamespace;

	public ServletWebServerApplicationContext() {
		super();
	}

	public ServletWebServerApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
	}

	@Override
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		beanFactory.addBeanPostProcessor(new WebApplicationContextServletContextAwareProcessor(this));
		beanFactory.ignoreDependencyInterface(ServletContextAware.class);
		registerWebApplicationScopes();
	}

	/**
	 * 注册web相关scope和已解析的依赖
	 */
	private void registerWebApplicationScopes() {
		DebugUtils.log(logger, "注册web相关scope和已解析的依赖(未完成)，by method：registerWebApplicationScopes()");
	}

	@Override
	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	@Override
	public ServletConfig getServletConfig() {
		return this.servletConfig;
	}

	@Override
	public WebServer getWebServer() {
		return this.webServer;
	}

	@Override
	public final void refresh() throws BeansException, IllegalStateException {
		try {
			super.refresh();
		} catch (RuntimeException ex) {
			stopAndReleaseWebServer();
			throw ex;
		}
	}

	/**
	 * 停止web服务器
	 */
	private void stopAndReleaseWebServer() {
		if (this.webServer != null) {
			try {
				this.webServer.stop();
				this.webServer = null;
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	@Override
	protected void onRefresh() {
		super.onRefresh();
		try {
			createWebServer();
		} catch (Throwable ex) {
			throw new ApplicationContextException("无法启动web服务器", ex);
		}
	}
	
	@Override
	protected void finishRefresh() {
		super.finishRefresh();
		WebServer webServer = startWebServer();
		if (webServer != null) {
			publishEvent(new ServletWebServerInitializedEvent(webServer, this));
		}
	}

	@Override
	protected void onClose() {
		super.onClose();
		stopAndReleaseWebServer();
	}
	
	private void createWebServer() {
		DebugUtils.log(logger, "创建Web服务器(未完成)，by method：createWebServer()");
	}

	private WebServer startWebServer() {
		WebServer webServer = this.webServer;
		if (webServer != null) {
			webServer.start();
		}
		return webServer;
	}
	
	@Override
	public String getServerNamespace() {
		return this.serverNamespace;
	}

	@Override
	public void setServerNamespace(String serverNamespace) {
		this.serverNamespace = serverNamespace;
	}
}

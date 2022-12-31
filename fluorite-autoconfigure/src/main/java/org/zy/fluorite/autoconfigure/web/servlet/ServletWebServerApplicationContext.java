package org.zy.fluorite.autoconfigure.web.servlet;

import java.util.Collection;
import java.util.EventListener;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.zy.fluorite.autoconfigure.utils.WebApplicationContextUtils;
import org.zy.fluorite.autoconfigure.web.server.WebServerStartStopLifecycle;
import org.zy.fluorite.autoconfigure.web.servlet.interfaces.ServletContextInitializer;
import org.zy.fluorite.autoconfigure.web.servlet.interfaces.ServletWebServerFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.context.exception.ApplicationContextException;
import org.zy.fluorite.context.support.DefaultListableBeanFactory;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.StringUtils;
import org.zy.fluorite.web.context.interfaces.WebApplicationContext;
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
public class ServletWebServerApplicationContext extends GenericWebApplicationContext implements WebServerApplicationContext {
	/**
	 * DispatcherServlet bean名称的常量值。 具有此名称的Servlet
	 * bean被视为“main”Servlet，默认情况下会自动给出“/”的映射。
	 */
	public static final String DISPATCHER_SERVLET_NAME = "dispatcherServlet";

	private final String ENABLE_WEB_SERVER = "server.servlet.enable";
	
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
			Boolean property = environment.getProperty(ENABLE_WEB_SERVER, boolean.class);
			if (property != null && !property) {
				if (logger.isInfoEnabled()) {
					logger.info("WebServer disabled due to System property '{}' being set to false", ENABLE_WEB_SERVER);
				}
				return ;
			}
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
	
	private void createWebServer() {
		DebugUtils.log(logger, "开始创建Web服务器.");
		
		WebServer webServer = this.webServer;
		ServletContext servletContext = getServletContext();
		if (webServer == null && servletContext == null) {
			ServletWebServerFactory factory = getWebServerFactory();
			this.webServer = factory.getWebServer(getSelfInitializer());
			
			getBeanFactory().registerSingleton("webServerStartStop", new WebServerStartStopLifecycle(this, this.webServer));
		} else if (servletContext != null) {
			try {
				getSelfInitializer().onStartup(servletContext);
			} catch (ServletException ex) {
				throw new ApplicationContextException("无法初始化 servlet 上下文", ex);
			}
		}
		if(logger.isInfoEnabled()) {
			logger.info("Web服务器创建完成.");
		}
	}

	/**
	 * 返回用于创建嵌入式web服务器的 {@link ServletWebServerFactory} 。默认情况下，该方法在上下文本身中搜索合适的bean。
	 * 
	 * @return 一个 {@link ServletWebServerFactory} (从不 {@code null})
	 */
	protected ServletWebServerFactory getWebServerFactory() {
		// 使用bean名称，以便不考虑层次结构
		String[] beanNames = getBeanFactory().getBeanNamesForType(ServletWebServerFactory.class);
		if (beanNames.length == 0) {
			throw new ApplicationContextException("由于缺少ServletWebServerFactory bean, 无法启动ServletWebServerApplicationContext. "
					+ "在 application.properties 中设置 'server.servlet.enable' 属性为 'false' 可禁用Fluorite的Servlet容器支持");
		}
		if (beanNames.length > 1) {
			throw new ApplicationContextException("由于存在多个ServletWebServerFactory bean，无法启动ServletWebSServerApplicationContext : " + StringUtils.append(", ", beanNames));
		}
		return getBeanFactory().getBean(beanNames[0], ServletWebServerFactory.class);
	}
	
	/**
	 * 返回将用于完成此 {@link WebApplicationContext} 的设置的 {@link ServletContextInitializer}。
	 * 
	 * @return ServletContextInitializer 对象
	 * @see #prepareWebApplicationContext(ServletContext)
	 */
	private ServletContextInitializer getSelfInitializer() {
		return this::selfInitialize;
	}
	
	private void selfInitialize(ServletContext servletContext) throws ServletException {
		prepareWebApplicationContext(servletContext);
		
//		registerApplicationScope(servletContext);
		WebApplicationContextUtils.registerEnvironmentBeans(getBeanFactory(), servletContext);
		
		for (ServletContextInitializer beans : getServletContextInitializerBeans()) {
			beans.onStartup(servletContext);
		}
	}
	
	/**
	 * 用给定的完全加载的 {@link ServletContext} 准备 {@link WebApplicationContext} 。
	 * 这个方法通常从 {@link ServletContextInitializer#onStartup(ServletContext)} 调用，类似于通常由 {@link ContextLoaderListener} 提供的功能。
	 * 
	 * @param servletContext - 待操作的servlet上下文
	 */
	protected void prepareWebApplicationContext(ServletContext servletContext) {
		Object rootContext = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (rootContext != null) {
			if (rootContext == this) {
				throw new IllegalStateException("无法初始化上下文, 因为已经存在一个根应用程序上下文——检查是否有多个ServletContextInitializers!");
			}
			return;
		}
		servletContext.log("正在初始化嵌入的WebApplicationContext");
		try {
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this);

			if (logger.isDebugEnabled()) {
				logger.debug("已将根WebApplicationContext发布名称为 ["
						+ WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]的ServletContext属性");
			}
			setServletContext(servletContext);
			if (logger.isInfoEnabled()) {
				long elapsedTime = System.currentTimeMillis() - getStartupDate();
				logger.info("根WebApplicationContext: 初始化在 " + elapsedTime + " ms内完成");
			}
		} catch (RuntimeException | Error ex) {
			logger.error("上下文初始化失败", ex);
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex);
			throw ex;
		}
	}
	
	/**
	 * 返回应与嵌入式Web服务器一起使用的 {@link ServletContextInitializer}s 。
	 * 默认情况下，此方法将首先尝试查找 {@link ServletContextInitializer}、{@link Servlet}、{@link Filter}和某些{@link EventListener} bean。
	 * 
	 * @return servlet初始化器bean
	 */
	protected Collection<ServletContextInitializer> getServletContextInitializerBeans() {
		return new ServletContextInitializerBeans(getBeanFactory());
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

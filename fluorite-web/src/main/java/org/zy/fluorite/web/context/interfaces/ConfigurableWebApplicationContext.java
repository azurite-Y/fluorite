package org.zy.fluorite.web.context.interfaces;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;

/**
 * @DateTime 2020年6月18日 下午11:30:11;
 * @author zy(azurite-Y);
 * @Description
 */
public interface ConfigurableWebApplicationContext extends WebApplicationContext, ConfigurableApplicationContext {

	/**
	 * 引用上下文路径和/或servlet名称的ApplicationContext标识的前缀。
	 */
	String APPLICATION_CONTEXT_ID_PREFIX = WebApplicationContext.class.getName() + ":";

	/**
	 * 工厂中ServletConfig环境bean的名称
	 */
	String SERVLET_CONFIG_BEAN_NAME = "servletConfig";

	/**
	 * 为此web应用程序上下文设置ServletContext。
	 * 不会导致上下文初始化：需要在设置完所有配置属性后调用刷新。
	 */
	void setServletContext(ServletContext servletContext);

	/**
	 * 为此web应用程序上下文设置ServletConfig。只为属于特定Servlet的WebApplicationContext调用
	 */
	void setServletConfig(ServletConfig servletConfig);

	/**
	 * 如果有，则返回此web应用程序上下文的ServletConfig
	 */
	ServletConfig getServletConfig();

	/**
	 * 设置此web应用程序上下文的命名空间
	 */
	void setNamespace(String namespace);

	/**
	 * 返回此web应用程序上下文的命名空间（如果有）
	 */
	String getNamespace();
	
	/**
	 * 设置上下文的服务器命名空间
	 */
	void setServerNamespace(String serverNamespace);

	/**
	 * 设置配置文件路径
	 */
	void setConfigLocation(String configLocation);

	/**
	 * 设置配置文件路径
	 */
	void setConfigLocations(String... configLocations);

	/**
	 * 获得配置文件路径
	 */
	String[] getConfigLocations();
}

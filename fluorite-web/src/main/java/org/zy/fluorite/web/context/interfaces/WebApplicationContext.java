package org.zy.fluorite.web.context.interfaces;

import javax.servlet.ServletContext;

import org.zy.fluorite.context.interfaces.ApplicationContext;

/**
 * @DateTime 2020年6月18日 下午11:40:00;
 * @author zy(azurite-Y);
 * @Description 提供web应用程序配置的接口
 */
public interface WebApplicationContext extends ApplicationContext {
	/**
	 * 成功启动时要将根WebApplicationContext绑定到的上下文属性
	 */
	String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";

	/**
	 * 请求作用域的作用域标识符：“request”
	 */
	String SCOPE_REQUEST = "request";

	/**
	 * 请求作用域的作用域标识符："session".
	 */
	String SCOPE_SESSION = "session";

	/**
	 * 全局web应用程序作用域的作用域标识符：“application”。
	 */
	String SCOPE_APPLICATION = "application";

	/**
	 * 工厂中ServletContext环境bean的名称
	 */
	String SERVLET_CONTEXT_BEAN_NAME = "servletContext";

	/**
	 * 工厂中ServletContext init-params环境bean的名称
	 */
	String CONTEXT_PARAMETERS_BEAN_NAME = "contextParameters";

	/**
	 *工厂中ServletContext属性环境bean的名称
	 */
	String CONTEXT_ATTRIBUTES_BEAN_NAME = "contextAttributes";


	/**
	 * 返回此应用程序的标准ServletAPI ServletContext
	 */
	ServletContext getServletContext();
}

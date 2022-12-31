package org.zy.fluorite.autoconfigure.web.servlet.interfaces;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @dateTime 2021年12月24日;
 * @author zy(azurite-Y);
 * @description 接口，用于通过上下文编程方式配置Servlet 3.0+.
 */ 
@FunctionalInterface
public interface ServletContextInitializer {
	/**
	 * 使用初始化所必需的servlet、过滤器、监听器上下文参数和属性配置给定的ServletContext.
	 * @param servletContext 要初始化的{@code ServletContext}
	 * @throws ServletException 如果任何调用给定的{@code ServletContext}都抛出一个{@code ServletException} 
	 */
	void onStartup(ServletContext servletContext) throws ServletException;
}

package org.zy.fluorite.autoconfigure.web.mvc;

import org.zy.fluorite.context.annotation.EnableConfigurationProperties;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnClass;
import org.zy.fluorite.core.annotation.Configuration;
import org.zy.fluorite.core.annotation.Order;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.web.servlet.DispatcherServlet;

/**
 * @dateTime 2022年12月8日;
 * @author zy(azurite-Y);
 * @description {@link DispatcherServlet} 的自动配置。应该适用于已经存在嵌入式web服务器的独立应用程序。
 */
//@Configuration
//@Order(Ordered.HIGHEST_PRECEDENCE)
//@ConditionalOnClass(type = { "org.zy.moonstone.core.startup.Moonstone" })
//@EnableConfigurationProperties(WebMvcProperties.class)
public class DispatcherServletAutoConfiguration {
	/*
	 * 将映射到根URL“/”的DispatcherServlet的bean名称
	 */
	public static final String DEFAULT_DISPATCHER_SERVLET_BEAN_NAME = "dispatcherServlet";

	/*
	 * DispatcherServlet "/"的ServletRegistrationBean的bean名称
	 */
	public static final String DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME = "dispatcherServletRegistration";
}

package org.zy.fluorite.autoconfigure.utils;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.web.context.interfaces.ConfigurableWebApplicationContext;
import org.zy.fluorite.web.context.interfaces.WebApplicationContext;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description
 */
public class WebApplicationContextUtils {
	
	/**
	 * 使用给定的BeanFactory注册特定于web的环境bean（“contextParameters”、“contextAttribute”），如WebApplicationContext所使用的。
	 * 
	 * @param bf - 要配置的BeanFactory
	 * @param sc - 正在运行的ServletContext
	 */
	public static void registerEnvironmentBeans(ConfigurableListableBeanFactory bf, ServletContext sc) {
		registerEnvironmentBeans(bf, sc, null);
	}

	/**
	 * 使用给定的BeanFactory注册特定于web的环境bean（“contextParameters”、“contextAttribute”），如WebApplicationContext所使用的。
	 * 
	 * @param bf - 要配置的BeanFactory
	 * @param servletContext - 正在运行的ServletContext
	 * @param servletConfig - ServletConfig
	 */
	public static void registerEnvironmentBeans(ConfigurableListableBeanFactory bf, ServletContext servletContext, ServletConfig servletConfig) {
		if (servletContext != null && !bf.containsBean(WebApplicationContext.SERVLET_CONTEXT_BEAN_NAME)) {
			bf.registerSingleton(WebApplicationContext.SERVLET_CONTEXT_BEAN_NAME, servletContext);
		}

		if (servletConfig != null && !bf.containsBean(ConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME)) {
			bf.registerSingleton(ConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME, servletConfig);
		}

		if (!bf.containsBean(WebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME)) {
			// 存储 servletContext 和 servletConfig 当中设置的初始化参数
			Map<String, String> parameterMap = new HashMap<>();
			
			if (servletContext != null) {
				Enumeration<?> paramNameEnum = servletContext.getInitParameterNames();
				if (paramNameEnum != null ) {
					while (paramNameEnum.hasMoreElements()) {
						String paramName = (String) paramNameEnum.nextElement();
						parameterMap.put(paramName, servletContext.getInitParameter(paramName));
					}
				}
				
			}
			if (servletConfig != null) {
				Enumeration<?> paramNameEnum = servletConfig.getInitParameterNames();
				if (paramNameEnum != null ) {
					while (paramNameEnum.hasMoreElements()) {
						String paramName = (String) paramNameEnum.nextElement();
						parameterMap.put(paramName, servletConfig.getInitParameter(paramName));
					}
				}
			}
			bf.registerSingleton(WebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME, Collections.unmodifiableMap(parameterMap));
		}

		if (!bf.containsBean(WebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME)) {
			Map<String, Object> attributeMap = new HashMap<>();
			if (servletContext != null) {
				Enumeration<?> attrNameEnum = servletContext.getAttributeNames();
				if (attrNameEnum != null ) {
					while (attrNameEnum.hasMoreElements()) {
						String attrName = (String) attrNameEnum.nextElement();
						attributeMap.put(attrName, servletContext.getAttribute(attrName));
					}
				}
			}
			bf.registerSingleton(WebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME, Collections.unmodifiableMap(attributeMap));
		}
	}
}

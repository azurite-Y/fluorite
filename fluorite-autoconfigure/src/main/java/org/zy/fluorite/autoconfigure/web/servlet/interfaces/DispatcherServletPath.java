package org.zy.fluorite.autoconfigure.web.servlet.interfaces;

import org.zy.fluorite.autoconfigure.web.mvc.DispatcherServletAutoConfiguration;
import org.zy.fluorite.autoconfigure.web.servlet.ServletRegistrationBean;

/**
 * @dateTime 2022年12月8日;
 * @author zy(azurite-Y);
 * @description 需要 {@link DispatcherServletAutoConfiguration#DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME 默认} DispatcherServlet 路径详细信息的自动配置可以使用的接口。
 */
@FunctionalInterface
public interface DispatcherServletPath {

	/**
	 * 返回调度程序servlet的配置路径
	 * 
	 * @return 配置的路径
	 */
	String getPath();

	/**
	 * 返回相对于调度程序servlet路径的给定路径的形式。
	 * 
	 * @param path - 使之相对的路径
	 * @return 相对路径
	 */
	default String getRelativePath(String path) {
		String prefix = getPrefix();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return prefix + path;
	}

	/**
	 * 返回可以用作url前缀的路径的清理版本。生成的路径将没有结尾斜杠。
	 * 
	 * @return 前缀
	 * @see #getRelativePath(String)
	 */
	default String getPrefix() {
		String result = getPath();
		int index = result.indexOf('*');
		if (index != -1) {
			result = result.substring(0, index);
		}
		if (result.endsWith("/")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	/**
	 * 返回一个URL映射模式，该模式可与 {@link ServletRegistrationBean} 一起用于映射调度程序servlet。
	 * 
	 * @return 将路径作为servlet URL映射
	 */
	default String getServletUrlMapping() {
		if (getPath().equals("") || getPath().equals("/")) {
			return "/";
		}
		if (getPath().contains("*")) {
			return getPath();
		}
		if (getPath().endsWith("/")) {
			return getPath() + "*";
		}
		return getPath() + "/*";
	}

}

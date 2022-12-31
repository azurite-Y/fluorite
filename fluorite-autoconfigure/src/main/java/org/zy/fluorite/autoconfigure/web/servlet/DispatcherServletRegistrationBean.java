package org.zy.fluorite.autoconfigure.web.servlet;

import java.util.Collection;

import org.zy.fluorite.autoconfigure.web.servlet.interfaces.DispatcherServletPath;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.web.servlet.DispatcherServlet;

/**
 * @dateTime 2022年12月8日;
 * @author zy(azurite-Y);
 * @description  {@link ServletRegistrationBean} 用于自动配置的 {@link DispatcherServlet} 。同时注册servlet并公开 {@link DispatcherServletPath} 信息。 
 */
public class DispatcherServletRegistrationBean extends ServletRegistrationBean<DispatcherServlet> implements DispatcherServletPath {

	private final String path;

	/**
	 * 为给定的servlet和路径创建一个新的 {@link DispatcherServletRegistrationBean} 实例。
	 * 
	 * @param servlet - DispatcherServlet
	 * @param path - 调度程序servlet路径
	 */
	public DispatcherServletRegistrationBean(DispatcherServlet servlet, String path) {
		super(servlet);
		Assert.notNull(path, "Path 不能为 null");
		this.path = path;
		super.addUrlMappings(getServletUrlMapping());
	}

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public void setUrlMappings(Collection<String> urlMappings) {
		throw new UnsupportedOperationException("URL映射不能在DispatcherServlet注册上更改");
	}

	@Override
	public void addUrlMappings(String... urlMappings) {
		throw new UnsupportedOperationException("URL映射不能在DispatcherServlet注册上更改");
	}
}

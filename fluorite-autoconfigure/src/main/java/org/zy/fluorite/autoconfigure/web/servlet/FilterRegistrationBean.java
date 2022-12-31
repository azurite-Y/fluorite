package org.zy.fluorite.autoconfigure.web.servlet;

import javax.servlet.Filter;
import javax.servlet.ServletContext;

import org.zy.fluorite.autoconfigure.web.servlet.interfaces.ServletContextInitializer;
import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月8日;
 * @author zy(azurite-Y);
 * @description
 * 用于在Servlet 3.0+容器中注册 {@link Filter} 的 {@link ServletContextInitializer} 。
 * 类似于ServletContext提供的 {@link ServletContext#addFilter(String, Filter) 注册} 特性，但是采用了Spring Bean友好的设计。
 * <p>
 * {@link #setFilter(Filter) Filter} 必须在调用onStartup(ServletContext)之前指定。
 * 注册可以与URL模式和/或servlet相关联(通过{@link #setServletNames name}或通过 {@link #setServletRegistrationBeans ServletRegistrationBean})。
 * 当指定了noURL模式或servlet时，过滤器将关联到'/*'。如果没有指定过滤器名，则会推导出该过滤器名。
 * 
 * @param <T> - 注册的 {@link Filter} 类型
 * 
 * @see ServletContextInitializer
 * @see ServletContext#addFilter(String, Filter)
 * 
 */
public class FilterRegistrationBean<T extends Filter> extends AbstractFilterRegistrationBean<T> {

	private T filter;

	/**
	 * 创建一个 {@link FilterRegistrationBean} 实例.
	 */
	public FilterRegistrationBean() {
	}

	/**
	 * 创建一个新的 {@link FilterRegistrationBean} 实例，用于向指定的 {@link ServletRegistrationBean} 注册。
	 * 
	 * @param filter - 注册的 filter
	 * @param servletRegistrationBeans - 关联的 {@link ServletRegistrationBean}s
	 */
	public FilterRegistrationBean(T filter, ServletRegistrationBean<?>... servletRegistrationBeans) {
		super(servletRegistrationBeans);
		Assert.notNull(filter, "Filter must not be null");
		this.filter = filter;
	}

	@Override
	public T getFilter() {
		return this.filter;
	}

	/**
	 * 设置要注册的 filter
	 * @param filter -  the filter
	 */
	public void setFilter(T filter) {
		Assert.notNull(filter, "Filter 不能为 null");
		this.filter = filter;
	}

}

package org.zy.fluorite.autoconfigure.web.servlet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;

import org.zy.fluorite.autoconfigure.web.servlet.interfaces.ServletContextInitializer;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @dateTime 2022年12月8日;
 * @author zy(azurite-Y);
 * @description 在Servlet 3.0+容器中注册过滤器的抽象基本 {@link ServletContextInitializer}。
 */
public abstract class AbstractFilterRegistrationBean<T extends Filter> extends DynamicRegistrationBean<Dynamic> {
	private static final String[] DEFAULT_URL_MAPPINGS = { "/*" };

	private Set<ServletRegistrationBean<?>> servletRegistrationBeans = new LinkedHashSet<>();

	private Set<String> servletNames = new LinkedHashSet<>();

	private Set<String> urlPatterns = new LinkedHashSet<>();

	private EnumSet<DispatcherType> dispatcherTypes;

	private boolean matchAfter = false;

	/**
	 * 创建要向指定的 {@link ServletRegistrationBean} 注册的新实例
	 * 
	 * @param servletRegistrationBeans - 关联的 {@link ServletRegistrationBean}s
	 */
	AbstractFilterRegistrationBean(ServletRegistrationBean<?>... servletRegistrationBeans) {
		Assert.notNull(servletRegistrationBeans, "ServletRegistrationBeans 不能为 null");
		Collections.addAll(this.servletRegistrationBeans, servletRegistrationBeans);
	}

	/**
	 * 设置将在其上注册筛选器的 {@link ServletRegistrationBean}s。
	 * 
	 * @param servletRegistrationBeans - Servlet注册Bean
	 */
	public void setServletRegistrationBeans(Collection<? extends ServletRegistrationBean<?>> servletRegistrationBeans) {
		Assert.notNull(servletRegistrationBeans, "ServletRegistrationBeans must not be null");
		this.servletRegistrationBeans = new LinkedHashSet<>(servletRegistrationBeans);
	}

	/**
	 * 返回将向其注册筛选器的 {@link ServletRegistrationBean} 的可变集合。
	 * 
	 * @return ServletRegistrationBean 实例
	 * @see #setServletNames
	 * @see #setUrlPatterns
	 */
	public Collection<ServletRegistrationBean<?>> getServletRegistrationBeans() {
		return this.servletRegistrationBeans;
	}

	/**
	 * 为过滤器添加 {@link ServletRegistrationBean}
	 * 
	 * @param servletRegistrationBeans - 要添加的Servlet注册Bean
	 * @see #setServletRegistrationBeans
	 */
	public void addServletRegistrationBeans(ServletRegistrationBean<?>... servletRegistrationBeans) {
		Assert.notNull(servletRegistrationBeans, "ServletRegistrationBeans 不能为 null");
		Collections.addAll(this.servletRegistrationBeans, servletRegistrationBeans);
	}

	/**
	 * 设置将注册筛选器的Servlet名称。这将替换以前指定的任何Servlet名称。
	 * 
	 * @param servletNames - Servlet名称
	 * @see #setServletRegistrationBeans
	 * @see #setUrlPatterns
	 */
	public void setServletNames(Collection<String> servletNames) {
		Assert.notNull(servletNames, "ServletNames 不能为 null");
		this.servletNames = new LinkedHashSet<>(servletNames);
	}

	/**
	 * 返回一个可变的servlet名称集合，过滤器将针对该集合进行注册。
	 * 
	 * @return servlet名称集合
	 */
	public Collection<String> getServletNames() {
		return this.servletNames;
	}

	/**
	 * 为过滤器添加servlet名称。
	 * 
	 * @param servletNames - 要添加的servlet名称
	 */
	public void addServletNames(String... servletNames) {
		Assert.notNull(servletNames, "ServletNames 不能为 null");
		this.servletNames.addAll(Arrays.asList(servletNames));
	}

	/**
	 * 设置将针对其注册筛选器的URL模式。这将替换以前指定的任何URL模式。
	 * 
	 * @param urlPatterns - URL模式
	 * @see #setServletRegistrationBeans
	 * @see #setServletNames
	 */
	public void setUrlPatterns(Collection<String> urlPatterns) {
		Assert.notNull(urlPatterns, "UrlPatterns 不能为 null");
		this.urlPatterns = new LinkedHashSet<>(urlPatterns);
	}

	/**
	 * 返回一个可变的URL模式集合，如Servlet规范中所定义的，过滤器将针对其进行注册。
	 * 
	 * @return URL模式集合
	 */
	public Collection<String> getUrlPatterns() {
		return this.urlPatterns;
	}

	/**
	 * 添加Servlet规范中定义的URL模式，过滤器将根据该模式注册
	 * 
	 * @param urlPatterns - URL模式
	 */
	public void addUrlPatterns(String... urlPatterns) {
		Assert.notNull(urlPatterns, "UrlPatterns must not be null");
		Collections.addAll(this.urlPatterns, urlPatterns);
	}

	/**
	 * 使用指定元素 {@link #setDispatcherTypes(EnumSet) 设置调度程序类型 } 的方便方法 
	 * 
	 * @param first - 第一个调度程序类型
	 * @param rest - 其他调度程序类型
	 */
	public void setDispatcherTypes(DispatcherType first, DispatcherType... rest) {
		this.dispatcherTypes = EnumSet.of(first, rest);
	}

	/**
	 * 设置应与注册一起使用的调度程序类型。如果未指定类型，将根据 {@link #isAsyncSupported()} 的值推导出类型。
	 * 
	 * @param dispatcherTypes - 调度程序类型
	 */
	public void setDispatcherTypes(EnumSet<DispatcherType> dispatcherTypes) {
		this.dispatcherTypes = dispatcherTypes;
	}

	/**
	 * 设置是否应在ServletContext的任何声明的过滤器映射之后匹配过滤器映射。默认值为false，表示假设在ServletContext的任何声明的筛选器映射之前匹配筛选器。
	 * 
	 * @param matchAfter - 是否之后匹配过滤器映射
	 */
	public void setMatchAfter(boolean matchAfter) {
		this.matchAfter = matchAfter;
	}

	/**
	 * 如果过滤器映射应该在ServletContext的任何声明的过滤器映射之后匹配，则返回 true。
	 * 
	 * @return true则之后匹配过滤器映射
	 */
	public boolean isMatchAfter() {
		return this.matchAfter;
	}

	@Override
	protected String getDescription() {
		Filter filter = getFilter();
		Assert.notNull(filter, "Filter must not be null");
		return "filter " + getOrDeduceName(filter);
	}

	@Override
	protected Dynamic addRegistration(String description, ServletContext servletContext) {
		Filter filter = getFilter();
		return servletContext.addFilter(getOrDeduceName(filter), filter);
	}

	
	
	/**
	 * 配置注册设置。如果需要，子类可以重写此方法来执行额外的配置
	 * 
	 */
	@Override
	protected void configure(FilterRegistration.Dynamic registration) {
		super.configure(registration);
		
		EnumSet<DispatcherType> dispatcherTypes = this.dispatcherTypes;
		if (dispatcherTypes == null) {
			dispatcherTypes = EnumSet.of(DispatcherType.REQUEST);
		}
		Set<String> servletNames = new LinkedHashSet<>();
		for (ServletRegistrationBean<?> servletRegistrationBean : this.servletRegistrationBeans) {
			servletNames.add(servletRegistrationBean.getServletName());
		}
		servletNames.addAll(this.servletNames);
		if (servletNames.isEmpty() && this.urlPatterns.isEmpty()) {
			registration.addMappingForUrlPatterns(dispatcherTypes, this.matchAfter, DEFAULT_URL_MAPPINGS);
		} else {
			if (!servletNames.isEmpty()) {
				registration.addMappingForServletNames(dispatcherTypes, this.matchAfter, StringUtils.toStringArray(servletNames));
			}
			if (!this.urlPatterns.isEmpty()) {
				registration.addMappingForUrlPatterns(dispatcherTypes, this.matchAfter, StringUtils.toStringArray(this.urlPatterns));
			}
		}
	}

	/**
	 * 返回要注册的 {@link Filter}
	 * 
	 * @return the filter
	 */
	public abstract T getFilter();
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getOrDeduceName(this));
		if (this.servletNames.isEmpty() && this.urlPatterns.isEmpty()) {
			builder.append(" urls=").append(Arrays.toString(DEFAULT_URL_MAPPINGS));
		}
		else {
			if (!this.servletNames.isEmpty()) {
				builder.append(" servlets=").append(this.servletNames);
			}
			if (!this.urlPatterns.isEmpty()) {
				builder.append(" urls=").append(this.urlPatterns);
			}
		}
		builder.append(" order=").append(getOrder());
		return builder.toString();
	}
}

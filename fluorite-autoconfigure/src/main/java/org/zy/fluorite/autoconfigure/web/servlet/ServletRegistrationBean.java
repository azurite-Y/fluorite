package org.zy.fluorite.autoconfigure.web.servlet;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.zy.fluorite.autoconfigure.web.servlet.interfaces.ServletContextInitializer;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @dateTime 2022年12月8日;
 * @author zy(azurite-Y);
 * @description
 * 
 * 用于在Servlet 3.0+容器中注册 {@link Servlet} 的 {@link ServletContextInitializer} 。
 * 类似于 {@link ServletContext} 提供的 {@link ServletContext#addServlet(String, Servlet) 注册} 特性，但是采用了fluorite bean友好的设计。
 * <p>
 * 在调用onStartup之前必须指定servlet。URL映射可以使用setUrlMappings进行配置，也可以在映射到'/*'时省略(除非alwaysMapUrl设置为false)。
 * 如果没有指定servlet名称，则会推导出servlet名称。
 *
 * @param <T> - 要注册的 {@link Servlet} 类型
 * 
 * @see ServletContextInitializer
 * @see ServletContext#addServlet(String, Servlet)
 */
public class ServletRegistrationBean<T extends Servlet> extends DynamicRegistrationBean<ServletRegistration.Dynamic> {
	private static final String[] DEFAULT_MAPPINGS = { "/*" };

	private T servlet;

	private Set<String> urlMappings = new LinkedHashSet<>();

	private boolean alwaysMapUrl = true;

	/** 默认值代表延迟加载 */
	private int loadOnStartup = -1;

	private MultipartConfigElement multipartConfig;

	/**
	 * 创建一个新的 {@link ServletRegistrationBean} 实例
	 */
	public ServletRegistrationBean() {
	}

	/**
	 * 使用指定的 {@link Servlet} 和URL映射创建一个新的 {@link ServletRegistrationBean} 实例。
	 * 
	 * @param servlet - 映射的servlet
	 * @param urlMappings - 映射的url
	 */
	public ServletRegistrationBean(T servlet, String... urlMappings) {
		this(servlet, true, urlMappings);
	}

	/**
	 * 使用指定的 {@link Servlet} 和URL映射创建一个新的 {@link ServletRegistrationBean} 实例。
	 * 
	 * @param servlet - 映射的servlet
	 * @param alwaysMapUrl - 如果省略URL映射，则应替换为'/*'
	 * @param urlMappings - 映射的url
	 */
	public ServletRegistrationBean(T servlet, boolean alwaysMapUrl, String... urlMappings) {
		Assert.notNull(servlet, "Servlet 不能为 null");
		Assert.notNull(urlMappings, "UrlMappings 不能为 null");
		this.servlet = servlet;
		this.alwaysMapUrl = alwaysMapUrl;
		this.urlMappings.addAll(Arrays.asList(urlMappings));
	}

	/**
	 * 设置要注册的servlet。
	 * 
	 * @param servlet - 注册的Servlet
	 */
	public void setServlet(T servlet) {
		Assert.notNull(servlet, "Servlet 不能为 null");
		this.servlet = servlet;
	}

	/**
	 * 返回已注册的servlet
	 * 
	 * @return 已注册的servlet
	 */
	public T getServlet() {
		return this.servlet;
	}

	/**
	 * 为servlet设置URL映射。如果没有指定，映射将默认为'/'。这将替换之前指定的任何映射。
	 * 
	 * @param urlMappings - 要设置的映射
	 * @see #addUrlMappings(String...)
	 */
	public void setUrlMappings(Collection<String> urlMappings) {
		Assert.notNull(urlMappings, "UrlMappings 不能为 null");
		this.urlMappings = new LinkedHashSet<>(urlMappings);
	}

	/**
	 * 返回servlet的URL映射的可变集合，如servlet规范中定义的那样。
	 * 
	 * @return URL映射
	 */
	public Collection<String> getUrlMappings() {
		return this.urlMappings;
	}

	/**
	 * 为Servlet添加Servlet规范中定义的URL映射。
	 * 
	 * @param urlMappings - 要添加的映射
	 * @see #setUrlMappings(Collection)
	 */
	public void addUrlMappings(String... urlMappings) {
		Assert.notNull(urlMappings, "UrlMappings 不能为 null");
		this.urlMappings.addAll(Arrays.asList(urlMappings));
	}

	/**
	 * 设置 {@code loadOnStartup} 优先级
	 * 
	 * @param loadOnStartup - 如果启动时加载启用
	 * @see {@link ServletRegistration.Dynamic#setLoadOnStartup}
	 */
	public void setLoadOnStartup(int loadOnStartup) {
		this.loadOnStartup = loadOnStartup;
	}

	/**
	 * 设置 {@link MultipartConfigElement multi-part 配置}
	 * 
	 * @param multipartConfig - multi-part 配置设置或 {@code null}
	 */
	public void setMultipartConfig(MultipartConfigElement multipartConfig) {
		this.multipartConfig = multipartConfig;
	}

	/**
	 * 返回 {@link MultipartConfigElement multi-part 配置} 设置或 {@code null}
	 * 
	 * @return multipart 配置
	 */
	public MultipartConfigElement getMultipartConfig() {
		return this.multipartConfig;
	}

	@Override
	protected String getDescription() {
		Assert.notNull(this.servlet, "Servlet 不能为 null");
		return "servlet " + getServletName();
	}

	@Override
	protected ServletRegistration.Dynamic addRegistration(String description, ServletContext servletContext) {
		String name = getServletName();
		return servletContext.addServlet(name, this.servlet);
	}

	/**
	 * 配置注册设置。如果需要，子类可以重写此方法来执行额外的配置。
	 * 
	 * @param registration - 注册设置
	 */
	@Override
	protected void configure(ServletRegistration.Dynamic registration) {
		super.configure(registration);
		String[] urlMapping = StringUtils.toStringArray(this.urlMappings);
		if (urlMapping.length == 0 && this.alwaysMapUrl) {
			urlMapping = DEFAULT_MAPPINGS;
		}
		if ( Assert.notNull(urlMapping) ) {
			registration.addMapping(urlMapping);
		}
		
		registration.setLoadOnStartup(this.loadOnStartup);
		
		if (this.multipartConfig != null) {
			registration.setMultipartConfig(this.multipartConfig);
		}
	}

	/**
	 * 返回将要注册的servlet名称
	 * 
	 * @return servlet名称
	 */
	public String getServletName() {
		return getOrDeduceName(this.servlet);
	}

	@Override
	public String toString() {
		return getServletName() + " urls=" + getUrlMappings();
	}
}

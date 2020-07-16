package org.zy.fluorite.web.context.support;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.web.context.interfaces.ConfigurableWebApplicationContext;

/**
 * @DateTime 2020年6月19日 上午9:12:19;
 * @author zy(azurite-Y);
 * @Description
 */
public class WebApplicationContextServletContextAwareProcessor extends ServletContextAwareProcessor {
	private final ConfigurableWebApplicationContext webApplicationContext;
	
	public WebApplicationContextServletContextAwareProcessor(ConfigurableWebApplicationContext webApplicationContext) {
		Assert.notNull(webApplicationContext, "WebApplicationContext不能为null");
		this.webApplicationContext = webApplicationContext;
	}

	@Override
	protected ServletContext getServletContext() {
		ServletContext servletContext = this.webApplicationContext.getServletContext();
		return (servletContext != null) ? servletContext : super.getServletContext();
	}

	@Override
	protected ServletConfig getServletConfig() {
		ServletConfig servletConfig = this.webApplicationContext.getServletConfig();
		return (servletConfig != null) ? servletConfig : super.getServletConfig();
	}
}

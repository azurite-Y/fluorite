package org.zy.fluorite.web.context.support;

import javax.servlet.ServletContext;

import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.context.support.DefaultListableBeanFactory;
import org.zy.fluorite.context.support.GenericApplicationContext;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.web.context.interfaces.ConfigurableWebApplicationContext;
import org.zy.fluorite.web.context.interfaces.auare.ServletContextAware;

/**
 * @DateTime 2020年6月18日 下午11:29:06;
 * @author zy(azurite-Y);
 * @Description
 */
public abstract class GenericWebApplicationContext extends GenericApplicationContext	implements ConfigurableWebApplicationContext {
	protected ServletContext servletContext;
	
	public GenericWebApplicationContext() {
		super();
	}
	public GenericWebApplicationContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	public GenericWebApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
	}
	public GenericWebApplicationContext(DefaultListableBeanFactory beanFactory, ServletContext servletContext) {
		super(beanFactory);
		this.servletContext = servletContext;
	}


	/**
	 * Set the ServletContext that this WebApplicationContext runs in.
	 */
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	@Override
	public String getApplicationName() {
		return (this.servletContext != null ? this.servletContext.getContextPath() : "");
	}
	
	@Override
	public ConfigurableEnvironment createEnvironment() {
		return new StandardServletEnvironment();
	}

	@Override
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		if (this.servletContext != null) {
			beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext));
			beanFactory.ignoreDependencyInterface(ServletContextAware.class);
		}
		// 注册web相关scope和已解析依赖项
//		WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
		// 注册相关web环境所需的Bean
//		WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext);
	}
	
	@Override
	public void setNamespace(String namespace) {}
	
	@Override
	public void setConfigLocation(String configLocation) {}
	
	@Override
	public void setConfigLocations(String... configLocations) {}
	
	@Override
	public String getNamespace() {
		throw new UnsupportedOperationException("GenericWebApplicationContext不支持getNamespace()方法");
	}

	@Override
	public String[] getConfigLocations() {
		throw new UnsupportedOperationException("GenericWebApplicationContex不支持getConfigLocations()方法");
	}
}

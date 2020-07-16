package org.zy.fluorite.web.context.support;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.zy.fluorite.beans.factory.interfaces.processor.BeanPostProcessor;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.annotation.Order;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.web.context.interfaces.auare.ServletConfigAware;
import org.zy.fluorite.web.context.interfaces.auare.ServletContextAware;

/**
 * @DateTime 2020年6月19日 上午9:14:40;
 * @author zy(azurite-Y);
 * @Description 将ServletContext传递给实现ServletContextAware接口的bean
 */
@Order(Ordered.HIGHEST_PRECEDENCE - 20)
public class ServletContextAwareProcessor implements BeanPostProcessor {
	private ServletContext servletContext;
	private ServletConfig servletConfig;


	protected ServletContextAwareProcessor() {}
	public ServletContextAwareProcessor(ServletContext servletContext) {
		this(servletContext, null);
	}
	public ServletContextAwareProcessor(ServletConfig servletConfig) {
		this(null, servletConfig);
	}
	public ServletContextAwareProcessor(ServletContext servletContext, ServletConfig servletConfig) {
		this.servletContext = servletContext;
		this.servletConfig = servletConfig;
	}

	protected ServletContext getServletContext() {
		if (this.servletContext == null && getServletConfig() != null) {
			return getServletConfig().getServletContext();
		}
		return this.servletContext;
	}

	protected ServletConfig getServletConfig() {
		return this.servletConfig;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, BeanDefinition beanDefinition) throws BeansException {
		if (getServletContext() != null && bean instanceof ServletContextAware) {
			((ServletContextAware) bean).setServletContext(getServletContext());
		}
		if (getServletConfig() != null && bean instanceof ServletConfigAware) {
			((ServletConfigAware) bean).setServletConfig(getServletConfig());
		}
		return bean;
	}
}

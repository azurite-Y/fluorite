package org.zy.fluorite.autoconfigure.web.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.autoconfigure.web.servlet.interfaces.ServletContextInitializer;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description 基于Servlet 3.0+的注册bean的基类
 */
public abstract class RegistrationBean implements ServletContextInitializer, Ordered {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private int order = Ordered.LOWEST_PRECEDENCE;

	private boolean enabled = true;

	@Override
	public final void onStartup(ServletContext servletContext) throws ServletException {
		String description = getDescription();
		if (!isEnabled()) {
			logger.info(StringUtils.capitalize(description) + " 未注册(已禁用)");
			return;
		}
		register(description, servletContext);
	}

	/**
	 * 返回注册说明。例如“Servlet resourceServlet”
	 * 
	 * @return 注册说明
	 */
	protected abstract String getDescription();

	/**
	 * 在servlet上下文中注册这个bean
	 * 
	 * @param description - 正在注册项的描述
	 * @param servletContext - servlet上下文
	 */
	protected abstract void register(String description, ServletContext servletContext);

	/**
	 * 标志，表示已启用注册
	 * 
	 * @param enabled - 设置的启用标识
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * 返回启用标识
	 * 
	 * @return 如果启用（默认为true）
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * 设置注册bean的顺序
	 * 
	 * @param order - the order
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * 获取注册bean的顺序
	 * 
	 * @return the order
	 */
	@Override
	public int getOrder() {
		return this.order;
	}
}

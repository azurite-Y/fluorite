package org.zy.fluorite.web.context.interfaces.auare;

import javax.servlet.ServletContext;

import org.zy.fluorite.core.interfaces.Aware;

/**
 * @DateTime 2020年6月19日 上午12:01:27;
 * @author zy(azurite-Y);
 * @Description
 */
public interface ServletContextAware extends Aware {
	void setServletContext(ServletContext servletContext);
}

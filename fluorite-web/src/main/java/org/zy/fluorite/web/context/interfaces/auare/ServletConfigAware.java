package org.zy.fluorite.web.context.interfaces.auare;

import javax.servlet.ServletConfig;

import org.zy.fluorite.core.interfaces.Aware;

/**
 * @DateTime 2020年6月19日 上午12:01:36;
 * @author zy(azurite-Y);
 * @Description
 */
public interface ServletConfigAware extends Aware {
	void setServletConfig(ServletConfig servletConfig);
}

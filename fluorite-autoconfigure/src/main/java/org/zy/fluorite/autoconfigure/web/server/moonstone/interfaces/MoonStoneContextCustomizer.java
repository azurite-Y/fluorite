package org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces;

import org.zy.moonstone.core.interfaces.container.Context;

/**
 * @dateTime 2022年4月1日;
 * @author zy(azurite-Y);
 * @description 
 */
@FunctionalInterface
public interface MoonstoneContextCustomizer {
	
	/**
	 * Context 自定义
	 * 
	 * @param context - 自定义的 Context
	 */
	void customize(Context context);
}

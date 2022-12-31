package org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces;

import org.zy.moonStone.core.interfaces.container.Context;

/**
 * @dateTime 2022年4月1日;
 * @author zy(azurite-Y);
 * @description 
 */
@FunctionalInterface
public interface MoonStoneContextCustomizer {
	
	/**
	 * Context 自定义
	 * 
	 * @param connector - 自定义的 Context
	 */
	void customize(Context context);
}

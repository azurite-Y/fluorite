package org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces;

import org.zy.moonStone.core.connector.Connector;

/**
 * @dateTime 2022年4月1日;
 * @author zy(azurite-Y);
 * @description 可用于自定义 MoonStone {@link Connector } 的回调接口
 */
@FunctionalInterface
public interface MoonStoneConnectorCustomizer {
	
	/**
	 * Connector 自定义
	 * 
	 * @param connector - 自定义的 connector
	 */
	void customize(Connector connector);
	
}

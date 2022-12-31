package org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces;

import org.zy.moonStone.core.interfaces.connector.ProtocolHandler;
import org.zy.moonStone.core.interfaces.container.Context;

/**
 * @dateTime 2022年4月1日;
 * @author zy(azurite-Y);
 * @description 可用于自定义 MoonStone {@link Context} 的回调接口 
 */
@FunctionalInterface
public interface MoonStoneProtocolHandlerCustomizer<T extends ProtocolHandler> {
	
	/**
	 * 自定义上下文
	 * @param context - 自定义的上下文
	 */
	void customize(T protocolHandler);
	
}

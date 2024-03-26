package org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces;

import org.zy.moonstone.core.interfaces.connector.ProtocolHandler;
import org.zy.moonstone.core.interfaces.container.Context;

/**
 * @dateTime 2022年4月1日;
 * @author zy(azurite-Y);
 * @description 可用于自定义 Moonstone {@link Context} 的回调接口
 */
@FunctionalInterface
public interface MoonstoneProtocolHandlerCustomizer<T extends ProtocolHandler> {
	
	/**
	 * 自定义协议处理器
	 * @param protocolHandler - 自定义的协议处理器
	 */
	void customize(T protocolHandler);
	
}

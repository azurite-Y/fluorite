package org.zy.fluorite.autoconfigure.web.server.interfaces;

import org.zy.fluorite.web.server.interfaces.WebServer;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description 关闭 {@link WebServer } 的配置
 */
public enum Shutdown {
	/**
	 * Web服务器应支持正常关闭，允许完成主动请求。
	 */
	GRACEFUL,

	/**
	 * Web服务器应立即关闭
	 */
	IMMEDIATE;
}

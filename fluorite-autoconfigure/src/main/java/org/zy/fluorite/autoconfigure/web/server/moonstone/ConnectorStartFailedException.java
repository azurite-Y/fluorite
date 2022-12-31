package org.zy.fluorite.autoconfigure.web.server.moonstone;

import org.zy.fluorite.web.server.exception.WebServerException;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description
 */
public class ConnectorStartFailedException extends WebServerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1642021131380722034L;
	
	private final int port;

	/**
	 * 当 MoonStone 连接器启动失败时抛出 {@code ConnectorStartFailedException} ，例如由于 {@code port} 冲突或错误的SSL配置。
	 * 
	 * @param port - 端口
	 */
	public ConnectorStartFailedException(int port) {
		super("配置为在端口 " + port + " 上监听的连接器启动失败", null);
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}

}

package org.zy.fluorite.web.server.interfaces;

import org.zy.fluorite.web.server.exception.WebServerException;

/**
 * @DateTime 2020年6月19日 上午12:11:49;
 * @author zy(azurite-Y);
 * @Description
 */
public interface WebServer {
	/**
	 * 启动web服务器。在已启动的服务器上调用此方法无效。
	 * @throws WebServerException - 如果服务器无法启动
	 */
	void start() throws WebServerException;

	/**
	 * 停止web服务器。在已停止的服务器上调用此方法无效。
	 * @throws WebServerException - 如果服务器无法停止
	 */
	void stop() throws WebServerException;

	int getPort();
}

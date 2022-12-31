package org.zy.fluorite.autoconfigure.web.server.moonstone;

import org.zy.fluorite.web.server.exception.WebServerException;


/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description 当web服务器由于端口已经在使用而无法启动时，会抛出PortInUseException
 */
public class PortInUseException extends WebServerException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2886325875234779276L;
	
	private final int port;

	/**
	 * 为给定端口创建一个 {@code PortInUseException }
	 * 
	 * @param port - 正在使用的端口
	 */
	public PortInUseException(int port) {
		this(port, null);
	}

	/**
	 * 为给定端口创建一个 {@code PortInUseException }
	 * 
	 * @param port - 正在使用的端口
	 * @param cause - 异常的原因
	 */
	public PortInUseException(int port, Throwable cause) {
		super("Port " + port + " is already in use", cause);
		this.port = port;
	}

	/**
	 * 返回正在使用的端口
	 * 
	 * @return 正在使用的端口
	 */
	public int getPort() {
		return this.port;
	}
}

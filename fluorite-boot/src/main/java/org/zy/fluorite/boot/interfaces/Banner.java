package org.zy.fluorite.boot.interfaces;

import java.io.IOException;
import java.io.PrintStream;

import org.zy.fluorite.core.environment.interfaces.Environment;

/**
 * @DateTime 2020年6月26日 上午9:07:56;
 * @author zy(azurite-Y);
 * @Description 
 */
@FunctionalInterface
public interface Banner {
	/**
	 * 将横幅打印到指定的打印流
	 * @param environment - 配置环境
	 * @param sourceClass - 根启动类
	 * @param out - 打印流
	 * @throws IOException 
	 */
	void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) throws IOException;

	/** 用于配置横幅的可能值的枚举 */
	enum Mode {
		/** 禁止打印横幅 */
		OFF,

		/** 打印横幅到控制台	 */
		CONSOLE,

		/** 打印横幅到日志文件 */
		LOG
	}

}

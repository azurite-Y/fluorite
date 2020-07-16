package org.zy.fluorite.core.utils;

import java.util.function.Supplier;

import org.slf4j.Logger;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午1:41:15;
 * @Description
 */
public final class DebugUtils {
	public static boolean debug;
	public static boolean debugFromAop;
	
	/**
	 * 若附加条件成立且可现实Debug信息则打印日志
	 * 
	 * @param logger
	 * @param flag
	 * @param message
	 */
	public static void log(Logger logger, boolean flag, String message) {
		if (debug && flag)
			logger.info(message);
	}

	/**
	 * 若附加条件成立且可显示Debug信息则打印日志
	 * 
	 * @param logger
	 * @param flag - 附加条件
	 * @param message
	 */
	public static void log(Logger logger, boolean flag, Supplier<String> messageSupplier) {
		log(logger, flag, messageSupplier.get());
	}

	/**
	 * 若可显示Debug信息则打印日志
	 * 
	 * @param logger
	 * @param flag
	 * @param message
	 */
	public static void log(Logger logger, String message) {
		if (debug) 	logger.info(message);
	}

	/**
	 * 若可显示Aop相关的Debug信息则打印日志
	 * @param logger
	 * @param flag
	 * @param message
	 */
	public static void log(Logger logger, Supplier<String> messageSupplier) {
		log(logger, messageSupplier.get());
	}
	
	// --------------------------------------------------------------
	
	/**
	 * 若附加条件成立且可显示Debug信息则打印日志
	 * 
	 * @param logger
	 * @param flag - 附加条件
	 * @param message
	 */
	public static void logFromAop(Logger logger, boolean flag, Supplier<String> messageSupplier) {
		logFromAop(logger, flag, messageSupplier.get());
	}

	/**
	 * 若可显示Debug信息则打印日志
	 * 
	 * @param logger
	 * @param flag
	 * @param message
	 */
	public static void logFromAop(Logger logger, Supplier<String> messageSupplier) {
		logFromAop(logger, messageSupplier.get());
	}
	
	/**
	 * 若可显示Debug信息则打印日志
	 * 
	 * @param logger
	 * @param flag
	 * @param message
	 */
	public static void logFromAop(Logger logger, String message) {
		if (debugFromAop) 	logger.info(message);
	}

	/**
	 * 若附加条件成立且可现实Aop相关的Debug信息则打印日志
	 * 
	 * @param logger
	 * @param flag
	 * @param message
	 */
	public static void logFromAop(Logger logger, boolean flag, String message) {
		if (debugFromAop && flag)	logger.info(message);
	}
}

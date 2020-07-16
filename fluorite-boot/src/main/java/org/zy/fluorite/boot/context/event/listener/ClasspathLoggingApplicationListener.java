package org.zy.fluorite.boot.context.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.zy.fluorite.boot.context.event.ApplicationFailedEvent;
import org.zy.fluorite.context.event.ApplicationEvent;
import org.zy.fluorite.context.event.interfaces.GenericApplicationListener;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @DateTime 2020年6月26日 下午5:25:34;
 * @author zy(azurite-Y);
 * @Description 在程序启动和失败时的打印类路径为日志到到控制台
 */
public class ClasspathLoggingApplicationListener implements GenericApplicationListener {

	private static final int ORDER = LoggingApplicationListener.DEFAULT_ORDER + 1;

	private static final Logger logger = LoggerFactory.getLogger(ClasspathLoggingApplicationListener.class);
	
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (DebugUtils.debug) {
			if (event instanceof ApplicationEnvironmentPreparedEvent) {
				logger.info("程序启动成功，by classpath: " + getClasspath());
			}
			else if (event instanceof ApplicationFailedEvent) {
				logger.info("程序启动失败，by classpath: " + getClasspath());
			}
		}
	}

	private String getClasspath() {
		return ClassLoader.getSystemResource("").getPath();
	}

	@Override
	public boolean supportsEventType(ResolvableType eventType) {
		return isAssignableFrom(eventType.resolve(), ApplicationEnvironmentPreparedEvent.class , ApplicationFailedEvent.class);
	}

	@Override
	public int getOrder() {
		return ORDER;
	}
	
}

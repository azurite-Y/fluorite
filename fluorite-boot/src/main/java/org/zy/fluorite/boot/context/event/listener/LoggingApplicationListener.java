package org.zy.fluorite.boot.context.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.boot.FluoriteApplication;
import org.zy.fluorite.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.zy.fluorite.boot.context.event.ApplicationFailedEvent;
import org.zy.fluorite.boot.context.event.ApplicationPreparedEvent;
import org.zy.fluorite.boot.context.event.ApplicationReadyEvent;
import org.zy.fluorite.boot.context.event.ApplicationStartingEvent;
import org.zy.fluorite.context.event.ApplicationEvent;
import org.zy.fluorite.context.event.ContextClosedEvent;
import org.zy.fluorite.context.event.interfaces.GenericApplicationListener;
import org.zy.fluorite.context.interfaces.ApplicationContext;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @DateTime 2020年6月26日 下午5:21:13;
 * @author zy(azurite-Y);
 * @Description 日志输出监听器，根据监听不同的事件而输出不同的日志
 */
public class LoggingApplicationListener implements GenericApplicationListener {
	public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 20;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final Class<?>[] EVENT_TYPES = { ApplicationStartingEvent.class,
			ApplicationEnvironmentPreparedEvent.class, ApplicationPreparedEvent.class, ContextClosedEvent.class,
			ApplicationFailedEvent.class , ApplicationReadyEvent.class };

	private static final Class<?>[] SOURCE_TYPES = { FluoriteApplication.class, ApplicationContext.class };

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationStartingEvent) {
			onApplicationStartingEvent((ApplicationStartingEvent) event);
		} else if (event instanceof ApplicationEnvironmentPreparedEvent) {
			onApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) event);
		} else if (event instanceof ApplicationPreparedEvent) {
			onApplicationPreparedEvent((ApplicationPreparedEvent) event);
		} else if (event instanceof ContextClosedEvent && ((ContextClosedEvent) event).getApplicationContext().getParent() == null) {
			onContextClosedEvent((ContextClosedEvent)event);
		} else if (event instanceof ApplicationFailedEvent) {
			onApplicationFailedEvent((ApplicationFailedEvent)event);
		} else if (event instanceof ApplicationReadyEvent) {
			onApplicationReadyEvent((ApplicationReadyEvent)event);
		}
	}

	private void onApplicationReadyEvent(ApplicationReadyEvent event) {
		logger.info("应用程序已启动完毕......");
		
	}

	private void onApplicationFailedEvent(ApplicationFailedEvent event) {
		logger.error("应用程序启动失败。");
	}

	private void onContextClosedEvent(ContextClosedEvent event) {
		logger.info("关闭应用程序上下文。");
	}

	private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
		logger.info("应用程序上下文属性填充。");
	}

	private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
		logger.info("应用程序配置环境属性填充。");
	}

	private void onApplicationStartingEvent(ApplicationStartingEvent event) {
		logger.info("应用程序启动。"); 
	}

	@Override
	public int getOrder() {
		return DEFAULT_ORDER;
	}
	
	@Override
	public boolean supportsEventType(ResolvableType eventType) {
		return isAssignableFrom(eventType.resolve(), EVENT_TYPES);
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return isAssignableFrom(sourceType, SOURCE_TYPES);
	}
}

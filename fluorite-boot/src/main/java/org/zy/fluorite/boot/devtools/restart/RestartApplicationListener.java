package org.zy.fluorite.boot.devtools.restart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.boot.context.event.ApplicationFailedEvent;
import org.zy.fluorite.boot.context.event.ApplicationPreparedEvent;
import org.zy.fluorite.boot.context.event.ApplicationReadyEvent;
import org.zy.fluorite.boot.context.event.ApplicationStartingEvent;
import org.zy.fluorite.context.event.ApplicationEvent;
import org.zy.fluorite.context.event.interfaces.ApplicationListener;
import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @dateTime 2022年12月28日;
 * @author zy(azurite-Y);
 * @description {@link ApplicationListener} to initialize the {@link Restarter}.
 */
public class RestartApplicationListener implements ApplicationListener<ApplicationEvent>, Ordered {

	private static final String ENABLED_PROPERTY = "spring.devtools.restart.enabled";

	private static final Logger logger = LoggerFactory.getLogger(RestartApplicationListener.class);

	private int order = HIGHEST_PRECEDENCE;

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationStartingEvent) {
			onApplicationStartingEvent((ApplicationStartingEvent) event);
		}
		if (event instanceof ApplicationPreparedEvent) {
			onApplicationPreparedEvent((ApplicationPreparedEvent) event);
		}
		if (event instanceof ApplicationReadyEvent || event instanceof ApplicationFailedEvent) {
			Restarter.getInstance().finish();
		}
		if (event instanceof ApplicationFailedEvent) {
			onApplicationFailedEvent((ApplicationFailedEvent) event);
		}
	}

	private void onApplicationStartingEvent(ApplicationStartingEvent event) {
		// 现在使用 Fluorite 环境还为时过早，但仍然应该允许用户使用System属性禁用重启。
		String enabled = System.getProperty(ENABLED_PROPERTY);
		
		if (enabled == null || Boolean.parseBoolean(enabled)) {
			String[] args = event.getArgs();
			DefaultRestartInitializer initializer = new DefaultRestartInitializer();
			Restarter.initialize(args, initializer, false);
		}
		else {
			logger.info("Restart disabled due to System property '{}' being set to false", ENABLED_PROPERTY);
			Restarter.disable();
		}
	}

	private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
		Restarter.getInstance().prepare(event.getApplicationContext());
	}

	private void onApplicationFailedEvent(ApplicationFailedEvent event) {
		Restarter.getInstance().remove(event.getApplicationContext());
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * Set the order of the listener.
	 * @param order the order of the listener
	 */
	public void setOrder(int order) {
		this.order = order;
	}

}

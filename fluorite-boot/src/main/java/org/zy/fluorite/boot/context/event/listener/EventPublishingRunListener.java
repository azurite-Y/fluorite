package org.zy.fluorite.boot.context.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.boot.FluoriteApplication;
import org.zy.fluorite.boot.context.event.ApplicationContextInitializedEvent;
import org.zy.fluorite.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.zy.fluorite.boot.context.event.ApplicationFailedEvent;
import org.zy.fluorite.boot.context.event.ApplicationPreparedEvent;
import org.zy.fluorite.boot.context.event.ApplicationReadyEvent;
import org.zy.fluorite.boot.context.event.ApplicationStartedEvent;
import org.zy.fluorite.boot.context.event.ApplicationStartingEvent;
import org.zy.fluorite.boot.interfaces.FluoriteApplicationRunListener;
import org.zy.fluorite.context.event.SimpleApplicationEventMulticaster;
import org.zy.fluorite.context.event.interfaces.ApplicationListener;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;
import org.zy.fluorite.context.interfaces.aware.ApplicationContextAware;
import org.zy.fluorite.context.support.AbstractApplicationContext;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @DateTime 2020年6月18日 下午2:26:02;
 * @author zy(azurite-Y);
 * @Description
 */
public class EventPublishingRunListener implements FluoriteApplicationRunListener, Ordered {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final FluoriteApplication application;

	private final String[] args;

	private final SimpleApplicationEventMulticaster initialMulticaster;

	public EventPublishingRunListener(FluoriteApplication application, String[] args) {
		super();
		this.application = application;
		this.args = args;
		this.initialMulticaster = new SimpleApplicationEventMulticaster();
		for (ApplicationListener<?> listener : application.getListeners()) {
			this.initialMulticaster.addApplicationListener(listener);
		}
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public void starting() {
		this.initialMulticaster.multicastEvent(new ApplicationStartingEvent(this.application, this.args));
	}

	@Override
	public void environmentPrepared(ConfigurableEnvironment environment) {
		this.initialMulticaster
				.multicastEvent(new ApplicationEnvironmentPreparedEvent(this.application, this.args, environment));
	}

	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {
		this.initialMulticaster
				.multicastEvent(new ApplicationContextInitializedEvent(this.application, this.args, context));
	}

	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {
		for (ApplicationListener<?> listener : this.application.getListeners()) {
			if (listener instanceof ApplicationContextAware) {
				((ApplicationContextAware) listener).setApplicationContext(context);
			}
			context.addApplicationListener(listener);
		}
		this.initialMulticaster.multicastEvent(new ApplicationPreparedEvent(this.application, this.args, context));
	}

	@Override
	public void started(ConfigurableApplicationContext context) {
		context.publishEvent(new ApplicationStartedEvent(this.application, this.args, context));
	}

	@Override
	public void running(ConfigurableApplicationContext context) {
		context.publishEvent(new ApplicationReadyEvent(this.application, this.args, context));
	}

	@Override
	public void failed(ConfigurableApplicationContext context, Throwable exception) {
		ApplicationFailedEvent event = new ApplicationFailedEvent(this.application, this.args, context, exception);
		if (context != null && context.isActive()) {
			context.publishEvent(event);
		} else {
			// 非活动上下文可能没有ApplicationEventMulticaster，因此使用本类封装FluoriteApplication所持有监听器的ApplicationEventMulticaster来调用上下文的所有侦听器
			if (context instanceof AbstractApplicationContext) {
				for (ApplicationListener<?> listener : ((AbstractApplicationContext) context)
						.getApplicationListeners()) {
					this.initialMulticaster.addApplicationListener(listener);
				}
			}
			this.initialMulticaster.setErrorHandler(throwable -> {
				logger.warn("调用ApplicationEventListener时出错", throwable);
			});
			this.initialMulticaster.multicastEvent(event);
		}
	}
}

package org.zy.fluorite.boot.context.event;

import org.zy.fluorite.boot.FluoriteApplication;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;

/**
 * @DateTime 2020年6月25日 下午11:55:50;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class ApplicationFailedEvent extends FluoriteApplicationEvent {
	private final ConfigurableApplicationContext context;

	private final Throwable exception;

	public ApplicationFailedEvent(FluoriteApplication application, String[] args, ConfigurableApplicationContext context,
			Throwable exception) {
		super(application, args);
		this.context = context;
		this.exception = exception;
	}

	public ConfigurableApplicationContext getApplicationContext() {
		return this.context;
	}

	public Throwable getException() {
		return this.exception;
	}
}

package org.zy.fluorite.boot.context.event;

import org.zy.fluorite.boot.FluoriteApplication;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;

/**
 * @DateTime 2020年6月25日 下午11:53:09;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class ApplicationStartedEvent extends FluoriteApplicationEvent {
	private final ConfigurableApplicationContext context;

	public ApplicationStartedEvent(FluoriteApplication application, String[] args,ConfigurableApplicationContext context) {
		super(application, args);
		this.context = context;
	}

	public ConfigurableApplicationContext getApplicationContext() {
		return this.context;
	}
}

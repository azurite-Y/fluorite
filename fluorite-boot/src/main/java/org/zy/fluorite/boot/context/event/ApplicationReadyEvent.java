package org.zy.fluorite.boot.context.event;

import org.zy.fluorite.boot.FluoriteApplication;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;

/**
 * @DateTime 2020年6月25日 下午11:54:43;
 * @author zy(azurite-Y);
 * @Description 程序已完全启动
 */
@SuppressWarnings("serial")
public class ApplicationReadyEvent extends FluoriteApplicationEvent {
	private final ConfigurableApplicationContext context;

	public ApplicationReadyEvent(FluoriteApplication application, String[] args, ConfigurableApplicationContext context) {
		super(application, args);
		this.context = context;
	}

	public ConfigurableApplicationContext getApplicationContext() {
		return this.context;
	}
}

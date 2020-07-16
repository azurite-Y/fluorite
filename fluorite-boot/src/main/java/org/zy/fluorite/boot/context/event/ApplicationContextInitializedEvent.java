package org.zy.fluorite.boot.context.event;

import org.zy.fluorite.boot.FluoriteApplication;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;
import org.zy.fluorite.core.annotation.Configuration;

/**
 * @DateTime 2020年6月25日 下午11:31:08;
 * @author zy(azurite-Y);
 * @Description ApplicationContext初始化时触发
 */
@SuppressWarnings("serial")
@Configuration
public class ApplicationContextInitializedEvent extends FluoriteApplicationEvent {

	private final ConfigurableApplicationContext context;

	public ApplicationContextInitializedEvent(FluoriteApplication application, String[] args,ConfigurableApplicationContext context) {
		super(application, args);
		this.context = context;
	}

	public ConfigurableApplicationContext getApplicationContext() {
		return this.context;
	}
}

package org.zy.fluorite.boot.context.event;

import org.zy.fluorite.boot.FluoriteApplication;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;

/**
 * @DateTime 2020年6月25日 下午11:50:54;
 * @author zy(azurite-Y);
 * @Description 在加载应用程序上下文但在重新刷新之前触发
 */
@SuppressWarnings("serial")
public class ApplicationPreparedEvent extends FluoriteApplicationEvent {
	private final ConfigurableApplicationContext context;
	
	public ApplicationPreparedEvent(FluoriteApplication application, String[] args, ConfigurableApplicationContext context) {
		super(application, args);
		this.context = context;
	}

	public ConfigurableApplicationContext getApplicationContext() {
		return this.context;
	}
}

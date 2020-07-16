package org.zy.fluorite.boot.context.event;

import org.zy.fluorite.boot.FluoriteApplication;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;

/**
 * @DateTime 2020年6月25日 下午11:28:59;
 * @author zy(azurite-Y);
 * @Description 
 */
@SuppressWarnings("serial")
public class ApplicationEnvironmentPreparedEvent extends FluoriteApplicationEvent {

	private final ConfigurableEnvironment environment;
	
	public ApplicationEnvironmentPreparedEvent(FluoriteApplication application, String[] args,
			ConfigurableEnvironment environment) {
		super(application, args);
		this.environment = environment;
	}

	public ConfigurableEnvironment getEnvironment() {
		return environment;
	}
}

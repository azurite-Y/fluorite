package org.zy.fluorite.core.interfaces;

import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;

/**
 * @DateTime 2020年6月17日 下午4:39:28;
 * @author zy(azurite-Y);
 * @Description 
 */
public interface EnvironmentAware {

	void setEnvironment(ConfigurableEnvironment environment);

}

package org.zy.fluorite.context.interfaces.aware;

import org.zy.fluorite.context.interfaces.ApplicationContext;

/**
 * @DateTime 2020年6月17日 下午4:40:18;
 * @author zy(azurite-Y);
 * @Description
 */
public interface ApplicationContextAware {

	void setApplicationContext(ApplicationContext applicationContext);

}

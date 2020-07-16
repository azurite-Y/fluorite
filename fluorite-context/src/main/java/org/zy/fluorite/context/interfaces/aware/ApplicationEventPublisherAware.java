package org.zy.fluorite.context.interfaces.aware;

import org.zy.fluorite.context.event.interfaces.ApplicationEventPublisher;

/**
 * @DateTime 2020年6月17日 下午4:44:44;
 * @author zy(azurite-Y);
 * @Description
 */
public interface ApplicationEventPublisherAware {

	void setApplicationEventPublisher(ApplicationEventPublisher publisher);

}

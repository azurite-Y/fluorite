package org.zy.fluorite.context.event.interfaces;

import org.zy.fluorite.context.event.ApplicationEvent;

/**
 * @DateTime 2020年6月17日 下午1:06:48;
 * @author zy(azurite-Y);
 * @Description 应用程序事件发布接口。
 */
@FunctionalInterface
public interface ApplicationEventPublisher {
	/**
	 * 将应用程序事件通知所有注册到此应用程序的匹配侦听器。
	 * 事件可以是框架事件（例如ContextRefreshedEvent）或特定于应用程序的事件。
	 */
	default void publishEvent(ApplicationEvent event) {
		publishEvent((Object) event);
	}

	/**
	 * 通知向此应用程序注册的所有匹配侦听器事件。
	 * 如果指定的事件不是ApplicationEvent，则将其包装在PayloadApplicationEvent中。
	 */
	void publishEvent(Object event);
}

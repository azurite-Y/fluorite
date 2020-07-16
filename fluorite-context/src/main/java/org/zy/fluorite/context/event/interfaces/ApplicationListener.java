package org.zy.fluorite.context.event.interfaces;

import java.util.EventListener;

import org.zy.fluorite.context.event.ApplicationEvent;

/**
 * @DateTime 2020年6月17日 下午1:57:27;
 * @author zy(azurite-Y);
 * @Description 要由应用程序事件侦听器实现的接口。
 */
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener  {
	/**
	 * 处理应用程序事件
	 * @param event
	 */
	void onApplicationEvent(E event);
}

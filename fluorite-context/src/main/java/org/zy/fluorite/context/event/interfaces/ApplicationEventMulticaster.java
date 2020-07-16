package org.zy.fluorite.context.event.interfaces;

import org.zy.fluorite.context.event.ApplicationEvent;
import org.zy.fluorite.core.convert.ResolvableType;

/**
 * @DateTime 2020年6月17日 下午2:22:32;
 * @author zy(azurite-Y);
 * @Description 将由对象实现的接口，这些对象可以管理多个ApplicationListener对象并向其发布事件。
 */
public interface ApplicationEventMulticaster {
	/**
	 * 添加一个侦听器，以接收所有事件的通知。
	 */
	void addApplicationListener(ApplicationListener<?> listener);

	/**
	 * 添加一个侦听器bean以获得所有事件的通知
	 */
	void addApplicationListenerBean(String listenerBeanName);

	/**
	 * 从通知列表中删除侦听器
	 */
	void removeApplicationListener(ApplicationListener<?> listener);

	/**
	 * 从通知列表中删除侦听器bean
	 */
	void removeApplicationListenerBean(String listenerBeanName);

	/**
	 * 删除所有注册到此多主机的侦听器。
	 * 在remove调用之后，在注册新的侦听器之前，multicaster不会执行action事件通知
	 */
	void removeAllListeners();

	/**
	 * 将给定的应用程序事件多播到适当的侦听器。
	 */
	void multicastEvent(ApplicationEvent event);

	/**
	 * 将给定的应用程序事件多播到适当的侦听器。
	 * 如果eventType为空，则根据事件实例构建默认类型。
	 */
	void multicastEvent(ApplicationEvent event, ResolvableType eventType);
}

package org.zy.fluorite.context.event;

import org.zy.fluorite.context.interfaces.ApplicationContext;

/**
 * @DateTime 2020年6月17日 下午2:59:43;
 * @author zy(azurite-Y);
 * @Description 上下文所触发的事件超类
 */
@SuppressWarnings("serial")
public abstract class ApplicationContextEvent extends ApplicationEvent {
	public ApplicationContextEvent(ApplicationContext source) {
		super(source);
	}
	public final ApplicationContext getApplicationContext() {
		return (ApplicationContext) getSource();
	}
}

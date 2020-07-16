package org.zy.fluorite.context.event;

import org.zy.fluorite.context.interfaces.ApplicationContext;

/**
 * @DateTime 2020年6月17日 下午5:30:53;
 * @author zy(azurite-Y);
 * @Description 由上下文的刷新而触发的事件
 */
@SuppressWarnings("serial")
public class ContextRefreshedEvent extends ApplicationContextEvent {

	public ContextRefreshedEvent(ApplicationContext source) {
		super(source);
	}
}

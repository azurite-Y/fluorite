package org.zy.fluorite.context.event;

import org.zy.fluorite.context.interfaces.ApplicationContext;

/**
 * @DateTime 2020年6月17日 下午2:58:31;
 * @author zy(azurite-Y);
 * @Description 上下文停止时触发此事件
 */
@SuppressWarnings("serial")
public class ContextStoppedEvent extends ApplicationContextEvent {
	
	public ContextStoppedEvent(ApplicationContext source) {
		super(source);
	}
	
}

package org.zy.fluorite.context.event;

import org.zy.fluorite.context.interfaces.ApplicationContext;

/**
 * @DateTime 2020年6月17日 下午2:58:31;
 * @author zy(azurite-Y);
 * @Description 上下文关闭时触发此事件
 */
@SuppressWarnings("serial")
public class ContextClosedEvent extends ApplicationContextEvent {
	
	public ContextClosedEvent(ApplicationContext source) {
		super(source);
	}
	
}

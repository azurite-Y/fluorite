package org.zy.fluorite.context.event;

import java.util.EventObject;

/**
 * @DateTime 2020年6月17日 下午1:06:58;
 * @author zy(azurite-Y);
 * @Description 包装事件信息的对象基类
 */
@SuppressWarnings("serial")
public abstract class ApplicationEvent  extends EventObject {
	/** 事件发生的系统时间 */
	private final long timestamp;
	
	/**
	 * @param source - 触发此事件的对象
	 */
	public ApplicationEvent(Object source) {
		super(source);
		timestamp = System.currentTimeMillis();
	}

	/**
	 * 获得事件发生的系统时间
	 * @return
	 */
	public final long getTimestamp() {
		return this.timestamp;
	}
}

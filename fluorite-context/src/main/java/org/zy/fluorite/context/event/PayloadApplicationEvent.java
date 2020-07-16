package org.zy.fluorite.context.event;

import org.zy.fluorite.core.convert.ResolvableType;

/**
 * @DateTime 2020年6月17日 下午1:17:50;
 * @author zy(azurite-Y);
 * @Description 包装非ApplicationEvent对象触发的事件
 */
@SuppressWarnings("serial")
public class PayloadApplicationEvent<T> extends ApplicationEvent {
	private final T payload;
	
	/**
	 * @param source - 一般为上下文对象
	 * @param payload - 触发事件的对象
	 */
	public PayloadApplicationEvent(Object source, T payload) {
		super(source);
		this.payload = payload;
	}

	public T getPayload() {
		return payload;
	}

	/**
	 * 根据payload属性的Class创建ResolvableType
	 * @return
	 */
	public ResolvableType getResolvableType() {
		return ResolvableType.forClass(this.payload.getClass());
	}
}

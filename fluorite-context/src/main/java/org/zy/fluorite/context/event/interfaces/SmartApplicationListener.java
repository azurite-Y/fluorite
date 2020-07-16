package org.zy.fluorite.context.event.interfaces;

import org.zy.fluorite.context.event.ApplicationEvent;
import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @DateTime 2020年6月18日 下午3:48:15;
 * @author zy(azurite-Y);
 * @Description 智能化的应用程序监听器接口，细粒度的进行事件与监听器匹配，提供对事件和触发事件的源对象进行匹配的方法
 */
public interface SmartApplicationListener extends ApplicationListener<ApplicationEvent>, Ordered {
	/**
	 * 确定此侦听器是否实际支持给定的事件类型
	 */
	boolean supportsEventType(Class<? extends ApplicationEvent> eventType);

	/**
	 * 确定此侦听器是否实际支持给定的源类型。默认实现总是返回true。
	 */
	default boolean supportsSourceType(Class<?> sourceType) {
		return true;
	}

	/**
	 * 确定此侦听器在同一事件的一组侦听器中的顺序。默认实现返回最低优先级。
	 */
	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}

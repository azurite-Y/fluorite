package org.zy.fluorite.context.event.interfaces;

import org.zy.fluorite.context.event.ApplicationEvent;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @DateTime 2020年6月18日 下午3:34:53;
 * @author zy(azurite-Y);
 * @Description 标准ApplicationListener接口的扩展变体，进一步公开元数据，如支持的事件和源类型
 */
public interface GenericApplicationListener extends ApplicationListener<ApplicationEvent>, Ordered  {
	/**
	 * 确定此侦听器是否实际支持给定的事件类型
	 */
	boolean supportsEventType(ResolvableType eventType);

	/**
	 * 确定此侦听器是否实际支持给定的事件类型
	 */
	default boolean supportsSourceType(Class<?> sourceType) {
		return true;
	}

	/**
	 * 确定此侦听器在同一事件的一组侦听器中的顺序。
	 * 默认实现返回最低优先级
	 */
	@Override
	default int getOrder() {
		return LOWEST_PRECEDENCE;
	}
	
	/**
	 * 判断指定类型是否是type类本身或是其父类
	 * @param type
	 * @param supportedTypes
	 * @return
	 */
	default boolean isAssignableFrom(Class<?> type, Class<?>... supportedTypes) {
		if (type != null) {
			for (Class<?> supportedType : supportedTypes) {
				if (supportedType.isAssignableFrom(type)) {
					return true;
				}
			}
		}
		return false;
	}
}

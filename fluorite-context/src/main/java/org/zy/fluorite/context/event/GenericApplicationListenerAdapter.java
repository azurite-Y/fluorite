package org.zy.fluorite.context.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.zy.fluorite.aop.utils.AopUtils;
import org.zy.fluorite.context.event.interfaces.ApplicationListener;
import org.zy.fluorite.context.event.interfaces.GenericApplicationListener;
import org.zy.fluorite.context.event.interfaces.SmartApplicationListener;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年6月18日 下午3:51:14;
 * @author zy(azurite-Y);
 * @Description
 */
public class GenericApplicationListenerAdapter implements GenericApplicationListener, SmartApplicationListener {
	private static final Map<Class<?>, ResolvableType> eventTypeCache = new ConcurrentHashMap<>();
	/** 委托监听器 */
	private final ApplicationListener<ApplicationEvent> delegate;
	/** 委托监听器监听的事件类型(泛型类型) */
	private final ResolvableType declaredEventType;
	
	
	@SuppressWarnings("unchecked")
	public GenericApplicationListenerAdapter(ApplicationListener<?> delegate) {
		Assert.notNull(delegate, "委托侦听器不能为空");
		this.delegate = (ApplicationListener<ApplicationEvent>) delegate;
		this.declaredEventType = resolveDeclaredEventType(this.delegate);
	}

	@Override
	public int getOrder() {
		return this.delegate instanceof Ordered ? ((Ordered) this.delegate).getOrder() : Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		this.delegate.onApplicationEvent(event);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean supportsEventType(ResolvableType eventType) {
		if (this.delegate instanceof SmartApplicationListener) {
			Class<? extends ApplicationEvent> eventClass = (Class<? extends ApplicationEvent>) eventType.resolve();
			return (eventClass != null && ((SmartApplicationListener) this.delegate).supportsEventType(eventClass));
		} else {
			return (this.declaredEventType == null || this.declaredEventType.isAssignableFrom(eventType));
		}
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return supportsEventType(ResolvableType.forClass(eventType));
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return !(this.delegate instanceof SmartApplicationListener) ||
				((SmartApplicationListener) this.delegate).supportsSourceType(sourceType);
	}

	/**
	 * 返回指定类型的泛型信息 
	 * @param listenerType
	 * @return
	 */
	private static ResolvableType resolveDeclaredEventType(ApplicationListener<ApplicationEvent> listener) {
		ResolvableType declaredEventType = resolveDeclaredEventType(listener.getClass());
		if (declaredEventType == null || declaredEventType.isAssignableFrom(ApplicationEvent.class)) {
			Class<?> targetClass = AopUtils.getTargetClass(listener);
			if (targetClass != listener.getClass()) {
				declaredEventType = resolveDeclaredEventType(targetClass);
			}
		}
		return declaredEventType;
	}
	
	/**
	 * 返回指定类型的泛型信息 
	 * @param listenerType
	 * @return
	 */
	static ResolvableType resolveDeclaredEventType(Class<?> listenerType) {
		ResolvableType eventType = eventTypeCache.get(listenerType);
		if (eventType == null) {
			eventType = ResolvableType.forClass(listenerType).as(ApplicationListener.class).getGeneric();
			eventTypeCache.put(listenerType, eventType);
		}
		return (eventType != ResolvableType.NONE ? eventType : null);
	}
}

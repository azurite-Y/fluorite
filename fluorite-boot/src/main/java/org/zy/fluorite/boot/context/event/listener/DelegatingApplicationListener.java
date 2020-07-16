package org.zy.fluorite.boot.context.event.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zy.fluorite.beans.support.AnnotationAwareOrderComparator;
import org.zy.fluorite.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.zy.fluorite.context.event.ApplicationEvent;
import org.zy.fluorite.context.event.SimpleApplicationEventMulticaster;
import org.zy.fluorite.context.event.interfaces.ApplicationListener;
import org.zy.fluorite.context.exception.ApplicationContextException;
import org.zy.fluorite.core.environment.Property;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年6月26日 下午5:27:46;
 * @author zy(azurite-Y);
 * @Description 读取“context.listener.classes”的属性，并将其value值实例化为应用程序监听器。
 *              然后添加到SimpleApplicationEventMulticaster中
 */
public class DelegatingApplicationListener implements ApplicationListener<ApplicationEvent>, Ordered {
	private int order = 0;

	private SimpleApplicationEventMulticaster multicaster;

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationEnvironmentPreparedEvent) {
			List<ApplicationListener<ApplicationEvent>> delegates = getListeners(
					((ApplicationEnvironmentPreparedEvent) event).getEnvironment());
			if (delegates.isEmpty()) {
				return;
			}
			this.multicaster = new SimpleApplicationEventMulticaster();
			for (ApplicationListener<ApplicationEvent> listener : delegates) {
				this.multicaster.addApplicationListener(listener);
			}
		}
		if (this.multicaster != null) {
			this.multicaster.multicastEvent(event);
		}
	}

	@SuppressWarnings("unchecked")
	private List<ApplicationListener<ApplicationEvent>> getListeners(ConfigurableEnvironment environment) {
		if (environment == null) {
			return Collections.emptyList();
		}
		List<String> classNames = environment.getPropertyToList(Property.LISTENER_CLASSES);
		List<ApplicationListener<ApplicationEvent>> listeners = new ArrayList<>();
		if (!classNames.isEmpty()) {
			for (String className : classNames) {
				try {
					Class<?> clazz = ReflectionUtils.forName(className);
					Assert.isAssignable(ApplicationListener.class, clazz);
					listeners.add((ApplicationListener<ApplicationEvent>) ReflectionUtils.instantiateClass(clazz));
				} catch (Exception ex) {
					throw new ApplicationContextException("加载预设的ApplicationListener实现失败，by： [" + className + "]", ex);
				}
			}
		}
		AnnotationAwareOrderComparator.sort(listeners);
		return listeners;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

}

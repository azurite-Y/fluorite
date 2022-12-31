package org.zy.fluorite.autoconfigure.web.servlet;

import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionListener;

import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月8日;
 * @author zy(azurite-Y);
 * @description
 */
public class ServletListenerRegistrationBean<T extends EventListener> extends RegistrationBean {
	private static final Set<Class<?>> SUPPORTED_TYPES;

	static {
		Set<Class<?>> types = new HashSet<>();
		types.add(ServletContextAttributeListener.class);
		types.add(ServletRequestListener.class);
		types.add(ServletRequestAttributeListener.class);
		types.add(HttpSessionAttributeListener.class);
		types.add(HttpSessionListener.class);
		types.add(ServletContextListener.class);
		SUPPORTED_TYPES = Collections.unmodifiableSet(types);
	}

	private T listener;

	/**
	 * 创建一个新的 {@link ServletListenerRegistrationBean} 实例
	 */
	public ServletListenerRegistrationBean() {
	}

	/**
	 * 创建一个新的 {@link ServletListenerRegistrationBean} 实例
	 * 
	 * @param listener - 注册的监听器
	 */
	public ServletListenerRegistrationBean(T listener) {
		Assert.notNull(listener, "Listener 不能为 null");
		Assert.isTrue(isSupportedType(listener), "Listener 不是支持的类型");
		this.listener = listener;
	}

	/**
	 * 设置注册的监听器
	 * @param listener - 要注册的侦听器
	 */
	public void setListener(T listener) {
		Assert.notNull(listener, "Listener 不能为 null");
		Assert.isTrue(isSupportedType(listener), "Listener 不是支持的类型");
		this.listener = listener;
	}

	/**
	 * 返回要注册的监听器
	 * 
	 * @return 要注册的监听器
	 */
	public T getListener() {
		return this.listener;
	}

	@Override
	protected String getDescription() {
		Assert.notNull(this.listener, "Listener 不能为 null");
		return "listener " + this.listener;
	}

	@Override
	protected void register(String description, ServletContext servletContext) {
		try {
			servletContext.addListener(this.listener);
		}
		catch (RuntimeException ex) {
			throw new IllegalStateException("添加 listener '" + this.listener + "' 到servlet上下文失败", ex);
		}
	}

	/**
	 * 如果指定的监听器是受支持的类型之一，则返回 {@code true}。
	 * 
	 * @param listener - 要测试的侦听器
	 * @return 如果侦听器是受支持的类型则为true
	 */
	public static boolean isSupportedType(EventListener listener) {
		for (Class<?> type : SUPPORTED_TYPES) {
			if (type.isAssignableFrom(listener.getClass())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 返回此注册所支持的类型
	 * 
	 * @return 支持的类型
	 */
	public static Set<Class<?>> getSupportedTypes() {
		return SUPPORTED_TYPES;
	}
}

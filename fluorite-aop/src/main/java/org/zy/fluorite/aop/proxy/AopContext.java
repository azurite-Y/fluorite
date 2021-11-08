package org.zy.fluorite.aop.proxy;

import org.zy.fluorite.core.subject.NamedThreadLocal;

/**
 * @DateTime 2020年7月4日 下午1:11:10;
 * @author zy(azurite-Y);
 * @Description
 */
public class AopContext {
	/**
	 * 与此线程关联的AOP代理的ThreadLocal持有者。除非控制proxyconfiguration上的“exposeProxy”属性设置为“true”，否则将包含null
	 * 
	 * @see ProxyConfig#setExposeProxy
	 */
	private static final ThreadLocal<Object> currentProxy = new NamedThreadLocal<>("Current AOP proxy");

	private AopContext() {}

	/**
	 * 尝试返回当前AOP代理。只有通过AOP调用了Calling方法，并且AOP框架已设置为公开代理时，此方法才可用。否则，此方法将引发AllegalStateException.
	 * 
	 * @return 当前AOP代理(从不返回{@code null})
	 * @throws IllegalStateException 如果由于在AOP调用上下文之外调用了该方法，或者由于AOP框架尚未配置为公开该代理而找不到该代理
	 */
	public static Object currentProxy() throws IllegalStateException {
		Object proxy = currentProxy.get();
		if (proxy == null) {
			throw new IllegalStateException("找不到当前代理：将Advised上的“exposeProxy”属性设置为“true”以使其可用，并确保在与AOP调用上下文相同的线程中调用AopContext.currentProxy()");
		}
		return proxy;
	}

	/**
	 * 通过currentProxy（）方法使给定代理可用.
	 * <p>
	 * 请注意，调用者应注意适当地保留旧值.
	 * 
	 * @param proxy 要公开的代理(或{@code null}重置它)
	 * @return 旧代理，如果没有绑定，则可能为{@code null}
	 * @see #currentProxy()
	 */
	static Object setCurrentProxy(Object proxy) {
		Object old = currentProxy.get();
		if (proxy != null) {
			currentProxy.set(proxy);
		} else {
			currentProxy.remove();
		}
		return old;
	}
}

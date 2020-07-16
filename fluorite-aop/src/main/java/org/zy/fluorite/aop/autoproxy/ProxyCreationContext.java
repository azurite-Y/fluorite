package org.zy.fluorite.aop.autoproxy;

import org.zy.fluorite.core.subject.NamedThreadLocal;

/**
 * @DateTime 2020年7月5日 上午9:55:22;
 * @author zy(azurite-Y);
 * @Description 当前代理创建上下文的持有者，由自动代理创建者（如AbstractAdvisorAutoProxyCreator）公开
 */
public class ProxyCreationContext {
	/** 在Advisor匹配期间，ThreadLocal保存当前代理的bean名称 */
	private static final ThreadLocal<String> currentProxiedBeanName = new NamedThreadLocal<>("代理Bean的名称");

	private ProxyCreationContext() {}

	public static String getCurrentProxiedBeanName() {
		return currentProxiedBeanName.get();
	}

	static void setCurrentProxiedBeanName(String beanName) {
		if (beanName != null) {
			currentProxiedBeanName.set(beanName);
		} else {
			currentProxiedBeanName.remove();
		}
	}
}

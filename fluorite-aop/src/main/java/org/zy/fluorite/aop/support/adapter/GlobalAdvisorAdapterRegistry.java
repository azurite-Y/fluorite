package org.zy.fluorite.aop.support.adapter;

import org.zy.fluorite.aop.interfaces.AdvisorAdapterRegistry;

/**
 * @DateTime 2020年7月5日 上午8:13:31;
 * @author zy(azurite-Y);
 * @Description 发布共享的DefaultAdvisorAdapterRegistry实例
 */
public class GlobalAdvisorAdapterRegistry {
	private GlobalAdvisorAdapterRegistry() {}

	private static AdvisorAdapterRegistry instance = new DefaultAdvisorAdapterRegistry();

	public static AdvisorAdapterRegistry getInstance() {
		return instance;
	}

	static void reset() {
		instance = new DefaultAdvisorAdapterRegistry();
	}
}

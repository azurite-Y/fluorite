package org.zy.fluorite.aop.interfaces;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 上午9:53:04;
 * @Description 被代理对象感知接口
 */
public interface TargetClassAware {
	/**
	 * 获得被代理对象的类型
	 * @return
	 */
	Class<?> getTargetClass();
}

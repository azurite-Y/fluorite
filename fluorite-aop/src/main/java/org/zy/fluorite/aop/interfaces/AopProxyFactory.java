package org.zy.fluorite.aop.interfaces;

import org.zy.fluorite.aop.exception.AopConfigException;
import org.zy.fluorite.aop.support.AdvisedSupport;

/**
 * @DateTime 2020年7月4日 下午4:44:43;
 * @author zy(azurite-Y);
 * @Description 接口由能够基于AdvisedSupport配置对象创建aop代理的工厂实现。
 */
public interface AopProxyFactory {
	/**
	 * 为给定的AOP配置创建AopProxy
	 * @param config - AdvisedSupport对象形式的AOP配置
	 * @return 相应的AOP代理
	 * @throws AopConfigException - 如果配置无效
	 */
	AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException;
}

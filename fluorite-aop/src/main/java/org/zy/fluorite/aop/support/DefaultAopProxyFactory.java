package org.zy.fluorite.aop.support;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import org.zy.fluorite.aop.exception.AopConfigException;
import org.zy.fluorite.aop.interfaces.AopProxy;
import org.zy.fluorite.aop.interfaces.AopProxyFactory;
import org.zy.fluorite.aop.interfaces.FluoriteProxy;
import org.zy.fluorite.aop.proxy.CglibAopProxy;
import org.zy.fluorite.aop.proxy.JdkDynamicAopProxy;

/**
 * @DateTime 2020年7月4日 下午4:56:48;
 * @author zy(azurite-Y);
 * @Description 默认的AopProxyFactory实现，根据情况决定创建CGLIB代理或JDK动态代理<br/>
 */
@SuppressWarnings("serial")
public class DefaultAopProxyFactory  implements AopProxyFactory, Serializable {
	
	/** 
	 * 确定提供的AdvisedSupport是否指定了 {@link FluoriteProxy }接口或根本没有指定代理接口
	 * @return 若未指定则返回true，指定了则返回false
	 */
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		Class<?>[] ifcs = config.getProxiedInterfaces();
		return (ifcs.length == 0 || (ifcs.length == 1 && FluoriteProxy.class.isAssignableFrom(ifcs[0])));
	}

	@Override
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		// 若指定了代理接口则使用JDk动态代理
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource无法确定目标类：创建代理需要接口或目标.");
			}
			// isProxyClass：仅当使用getProxyClassmethod或newProxyInstance方法将指定的类动态生成为代理类时返回true
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(config); // 使用JDK动态代理
			}
			return new CglibAopProxy(config); // 使用Cglib动态代理
		} else {
			return new JdkDynamicAopProxy(config);
		}
	}

}

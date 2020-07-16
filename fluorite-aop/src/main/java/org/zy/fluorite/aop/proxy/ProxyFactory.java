package org.zy.fluorite.aop.proxy;

import org.zy.fluorite.aop.interfaces.Interceptor;
import org.zy.fluorite.aop.interfaces.TargetSource;
import org.zy.fluorite.aop.support.ProxyCreatorSupport;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午5:15:02;
 * @Description
 */
@SuppressWarnings("serial")
public class ProxyFactory  extends ProxyCreatorSupport {
	public ProxyFactory() {
	}

	/**
	 * 创建新的代理工厂。将代理给定目标实现的所有接口.
	 * @param target - 被代理的对象
	 */
	public ProxyFactory(Object target) {
		setTarget(target);
		setInterfaces(ClassUtils.getAllInterfaces(target));
	}

	/**
	 * Create a new ProxyFactory.
	 * <p>No target, only interfaces. Must add interceptors.
	 * @param proxyInterfaces the interfaces that the proxy should implement
	 */
	public ProxyFactory(Class<?>... proxyInterfaces) {
		setInterfaces(proxyInterfaces);
	}

	/**
	 * 为给定的接口和拦截器创建一个新的代理工厂
	 * <p>
	 * 为单个拦截器创建代理的便利方法，假设拦截器自己处理所有调用，而不是像远程处理代理那样委托给目标
	 * @param proxyInterface - 代理应该实现的接口
	 * @param interceptor 代理应该调用的拦截器
	 */
	public ProxyFactory(Class<?> proxyInterface, Interceptor interceptor) {
		addInterface(proxyInterface);
		addAdvice(interceptor);
	}

	/**
	 * 为指定的TargetSource创建代理工厂，使代理实现指定的接口
	 * @param proxyInterface - 代理应该实现的接口
	 * @param targetSource - 代理应调用的TargetSource
	 */
	public ProxyFactory(Class<?> proxyInterface, TargetSource targetSource) {
		addInterface(proxyInterface);
		setTargetSource(targetSource);
	}


	/**
	 * 根据此工厂中的设置创建新代理。
	 * <p>可以重复调用。如果我们添加或删除接口，效果会有所不同。可以添加和删除拦截器。</p>
	 * 使用默认的类加载器：通常是线程上下文类加载器（如果需要创建代理）
	 * @return the proxy object
	 */
	public Object getProxy() {
		return createAopProxy().getProxy();
	}

	/**
	 * 根据此工厂中的设置创建新代理。
	 * <p>可以重复调用。如果我们添加或删除接口，效果会有所不同。可以添加和删除拦截器。</p>
	 * 使用给定的类加载器（如果需要创建代理）。
	 * @param classLoader - 用于创建代理的类加载器（或null表示低级代理设施的默认值）
	 * @return
	 */
	public Object getProxy(ClassLoader classLoader) {
		return createAopProxy().getProxy(classLoader);
	}


	/**
	 * 为给定的接口和拦截器创建一个新的代理。
	 * <p>为单个拦截器创建代理的便利方法，假设拦截器自己处理所有调用，而不是像远程处理代理那样委托给目标
	 * @param proxyInterface - 代理应该实现的接口
	 * @param interceptor - 代理应调用的TargetSource
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProxy(Class<T> proxyInterface, Interceptor interceptor) {
		return (T) new ProxyFactory(proxyInterface, interceptor).getProxy();
	}

	/**
	 * 为指定的TargetSource创建代理，实现指定的接口
	 * @param proxyInterface - 代理应该实现的接口
	 * @param interceptor - 代理应调用的TargetSource
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProxy(Class<T> proxyInterface, TargetSource targetSource) {
		return (T) new ProxyFactory(proxyInterface, targetSource).getProxy();
	}

	/**
	 * 为指定的TargetSource创建一个代理，该代理继承TargetSource的target类
	 * @param targetSource - 代理应调用的TargetSource
	 */
	public static Object getProxy(TargetSource targetSource) {
		Assert.notNull(targetSource.getTargetClass(),"无法为目标类为null的TargetSource创建类代理");
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTargetSource(targetSource);
		proxyFactory.setProxyTargetClass(true);
		return proxyFactory.getProxy();
	}
}

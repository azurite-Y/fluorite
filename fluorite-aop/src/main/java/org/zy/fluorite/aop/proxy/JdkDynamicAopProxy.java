package org.zy.fluorite.aop.proxy;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.aop.exception.AopConfigException;
import org.zy.fluorite.aop.exception.AopInvocationException;
import org.zy.fluorite.aop.interfaces.Advised;
import org.zy.fluorite.aop.interfaces.AopProxy;
import org.zy.fluorite.aop.interfaces.DecoratingProxy;
import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.TargetSource;
import org.zy.fluorite.aop.support.AdvisedSupport;
import org.zy.fluorite.aop.utils.AopProxyUtils;
import org.zy.fluorite.aop.utils.AopUtils;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年7月8日 下午4:07:15;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {
	private static final Logger logger = LoggerFactory.getLogger(JdkDynamicAopProxy.class);

	private final AdvisedSupport advised;

	/** {@link #equals}方法是否在代理接口上定义 */
	private boolean equalsDefined;

	/** {@link #hashCode}方法是否在代理接口上定义 */
	private boolean hashCodeDefined;

	public JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport不能为null");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("未指定advisor和TargetSource");
		}
		this.advised = config;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object oldProxy = null;
		boolean setProxyContext = false;

		TargetSource targetSource = this.advised.getTargetSource();
		Object target = null;

		try {
			if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
				// 目标本身未实现equals方法。
				return equals(args[0]);
			} else if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
				// 目标本身未实现hashCode()方法。
				return hashCode();
			} else if (method.getDeclaringClass() == DecoratingProxy.class) {
				return AopProxyUtils.ultimateTargetClass(this.advised);
			} else if (!this.advised.isOpaque() && method.getDeclaringClass().isInterface()
					&& method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				// 使用代理配置对ProxyConfig进行服务调用
				return ReflectionUtils.invokeMethod (this.advised, method, args);
			}
			Object methodReturn;
			if (this.advised.isExposeProxy()) {
				// 若advised中设置了公开代理，则在此保存旧代理
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}

			target = targetSource.getTarget();
			Class<?> targetClass = (target != null ? target.getClass() : null);

			// 获取此方法的拦截链
			List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
			if (chain.isEmpty()) {
				Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
				methodReturn = ReflectionUtils.invokeMethod(target, method, argsToUse);
			} else {
				MethodInvocation invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass,
						chain);
				// 通过拦截器链进入连接点
				methodReturn = invocation.proceed();
			}

			Class<?> returnType = method.getReturnType();
			if ( methodReturn != null && methodReturn == target && returnType != Object.class && returnType.isInstance(proxy) ) {
				methodReturn = proxy;
			} else if (methodReturn == null && returnType != Void.TYPE && returnType.isPrimitive()) {
				throw new AopInvocationException(
						"代理方法未返回原始方法需要返回的返回值，by method: " + method+"，需要返回的返回值类型："+returnType.getName());
			}
			return methodReturn;
		} finally {
			if (target != null && !targetSource.isStatic()) {
				targetSource.releaseTarget(target);
			}
			if (setProxyContext) {
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}

	@Override
	public Object getProxy(ClassLoader classLoader) {
		DebugUtils.logFromAop(logger, "创建JDK动态代理: " + this.advised.getTargetSource());
		Class<?>[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised, true);
		findDefinedEqualsAndHashCodeMethods(proxiedInterfaces);
		return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
	}

	/**
	 * 查找可能在提供的一组接口上定义的任何 {@link #equals} 或 {@link #hashCode} 方法
	 * 
	 * @param proxiedInterfaces - 检查的接口
	 */
	private void findDefinedEqualsAndHashCodeMethods(Class<?>[] proxiedInterfaces) {
		for (Class<?> proxiedInterface : proxiedInterfaces) {
			ReflectionUtils.doWithLocalMethods(proxiedInterface, method -> {
				if (AopUtils.isEqualsMethod(method)) {
					this.equalsDefined = true;
				}
				if (AopUtils.isHashCodeMethod(method)) {
					this.hashCodeDefined = true;
				}
				if (this.equalsDefined && this.hashCodeDefined) {
					return;
				}
			});
		}
	}

}

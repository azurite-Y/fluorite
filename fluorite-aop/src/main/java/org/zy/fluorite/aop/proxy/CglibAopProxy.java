package org.zy.fluorite.aop.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.aop.exception.AopConfigException;
import org.zy.fluorite.aop.exception.AopInvocationException;
import org.zy.fluorite.aop.interfaces.Advised;
import org.zy.fluorite.aop.interfaces.AopProxy;
import org.zy.fluorite.aop.interfaces.TargetSource;
import org.zy.fluorite.aop.support.AdvisedSupport;
import org.zy.fluorite.aop.target.SingletonTargetSource;
import org.zy.fluorite.aop.utils.AopProxyUtils;
import org.zy.fluorite.aop.utils.AopUtils;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;
import org.zy.fluorite.core.utils.DebugUtils;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

/**
 * @DateTime 2020年7月8日 下午4:07:03;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class CglibAopProxy implements AopProxy, Serializable {
	protected static final Logger logger = LoggerFactory.getLogger(CglibAopProxy.class);

	// CGLIB回调数组索引的常量
	/** 适配于普通方法的调用 */
	private static final int AOP_PROXY = 0;
	
	/** 适配于返回this的方法调用 */
	private static final int INVOKE_TARGET = 1;
	
	/** 适配于finalize()方法的调用 */
	private static final int NO_OVERRIDE = 2;
	
	/** 适配于没有Advice方法织入的方法调用，只是返回值需要检查 */
	private static final int DISPATCH_TARGET = 3;
	
	/** 特定于返回Advised实现的方法调用 */
	private static final int DISPATCH_ADVISED = 4;
	
	/** 适配于equals方法的调用 */
	private static final int INVOKE_EQUALS = 5;
	
	/** 适配于hashCode方法的调用 */
	private static final int INVOKE_HASHCODE = 6;
	
	/** 用来配置此代理的配置对象 */
	protected final AdvisedSupport advised;

	/** 用于返回值为Advised实现的方法 */
	protected final transient AdvisedDispatcher advisedDispatcher;
	
	public CglibAopProxy(AdvisedSupport config) {
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("未指定advisor和TargetSource");
		}
		this.advised = config;
		this.advisedDispatcher = new AdvisedDispatcher(this.advised);
	}

	@Override
	public Object getProxy(ClassLoader classLoader) {
		Class<?> rootClass = this.advised.getTargetClass();
		Assert.notNull(rootClass, "目标类必须可用于创建CGLIB代理");
		DebugUtils.logFromAop(logger, "开始使用Cglib创建代理对象：" + rootClass);

		// 被代理类对象
		Class<?> proxySuperClass = rootClass;
		if (rootClass.getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {
			proxySuperClass = rootClass.getSuperclass();
			Class<?>[] additionalInterfaces = rootClass.getInterfaces();
			for (Class<?> additionalInterface : additionalInterfaces) {
				this.advised.addInterface(additionalInterface);
			}
		}

		Enhancer enhancer = new Enhancer();
		classLoader = (classLoader == null ? ClassUtils.getDefaultClassLoader() : classLoader);
		enhancer.setClassLoader(classLoader);
		// 设置父类型
		enhancer.setSuperclass(proxySuperClass);

		enhancer.setInterfaces(AopProxyUtils.completeProxiedInterfaces(this.advised));

		Callback[] callbacks = getCallbacks(rootClass);
		Class<?>[] types = new Class<?>[callbacks.length];
		for (int x = 0; x < types.length; x++) {
			types[x] = callbacks[x].getClass();
		}
		enhancer.setCallbackFilter(new ProxyCallbackFilter( this.advised.getConfigurationOnlyCopy() ));
		enhancer.setCallbackTypes(types);
		// 生成代理类并创建代理实例
		return createProxyClassAndInstance(enhancer, callbacks);
	}

	private Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
		// 设置是否将拦截从代理的构造函数中调用的方法。默认值为true。未截获的方法将调用代理的基类的方法（如果存在）
		enhancer.setInterceptDuringConstruction(false);
		enhancer.setCallbacks(callbacks);
		return enhancer.create();
	}

	/**
	 * 因为源对象使用 {@linkplain SingletonTargetSource } 类包装，
	 * 所以此处创建的Callback对象特定于适用此对象
	 * @param rootClass
	 * @return
	 */
	private Callback[] getCallbacks(Class<?> rootClass) {
		// 负责切面通知方法调用
		Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);
		
		Callback targetInterceptor = new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget());
		
		Callback targetDispatcher = new StaticDispatcher(this.advised.getTargetSource().getTarget());
		
		Callback[] mainCallbacks = new Callback[] {
				aopInterceptor, // 普通方法分配器
				targetInterceptor, // 方法返回它自身时使用此Callback
				new SerializableNoOp(), // 特定于finalize()方法的调用而不做处理
				targetDispatcher,  // 没有Advice方法织入的方法调用，只是返回值需要检查
				this.advisedDispatcher, // 特定于返回Advised实现的方法调用
				new EqualsInterceptor(this.advised),
				new HashCodeInterceptor(this.advised)
		};
		return mainCallbacks;
	}

	/**
	 * 揣摩返回值，根据情况会对其修改
	 * @param proxy
	 * @param target
	 * @param method
	 * @param returnValue
	 * @return
	 */
	private static Object processReturnType(Object proxy, Object target, Method method, Object returnValue) {
		if (returnValue != null && returnValue == target ) {
			returnValue = proxy;
		}
		Class<?> returnType = method.getReturnType();
		// isPrimitive：当且仅当该类表示原始类型时为真，即八中基本数据类型
		if (returnValue == null && returnType != Void.TYPE && returnType.isPrimitive()) {
			throw new AopInvocationException(
					"代理方法未返回原始方法需要返回的返回值，by method: " + method+"，需要返回的返回值类型："+returnType.getName());
		}
		return returnValue;
	}
	
	/** 通用AOP回调。负责构造MethodInvocation并调用proceed方法。把对原始方法的调用委托给methodInvocation */
	private static class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {

		protected final AdvisedSupport advised;

		public DynamicAdvisedInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			boolean setProxyContext = false;
			Object target = null;
			TargetSource targetSource = this.advised.getTargetSource();
			try {
				if (this.advised.isExposeProxy()) {
					// 若配置类中设置了公开代理，则在此保存旧代理
					oldProxy = AopContext.setCurrentProxy(proxy);
					setProxyContext = true;
				}
				target = targetSource.getTarget();
				Class<?> targetClass = (target != null ? target.getClass() : null);
				List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
				Object retVal = null;
				// 若此方法不需要切面织入则之间反射调用，反之则创建MethodInvocation进行切面织入
				if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
					Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
					retVal = methodProxy.invoke(target, argsToUse);
				} else {
					// 创建MethodInvocation对象并调用其proceed()方法
					retVal = new CglibMethodInvocation(proxy, target, method, args, targetClass, chain, methodProxy).proceed();
				}
				retVal = processReturnType(proxy, target, method, retVal);
				return retVal;
			} finally {
				if (target != null && !targetSource.isStatic()) { // SingletonTargetSource不执行此逻辑
					targetSource.releaseTarget(target);
				}
				if (setProxyContext) {
					// 还原旧代理
					AopContext.setCurrentProxy(oldProxy);
				}
			}
		}

		@Override
		public boolean equals(Object other) {
			return (this == other || (other instanceof DynamicAdvisedInterceptor
					&& this.advised.equals(((DynamicAdvisedInterceptor) other).advised)));
		}

		@Override
		public int hashCode() {
			return this.advised.hashCode();
		}
	}

	/** MethodInvocation的Aop通知调用实现 */
	private static class CglibMethodInvocation extends ReflectiveMethodInvocation {

		private final MethodProxy methodProxy;

		public CglibMethodInvocation(Object proxy, Object target, Method method,
				Object[] arguments, Class<?> targetClass,
				List<Object> interceptorsAndDynamicMethodMatchers, MethodProxy methodProxy) {

			super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);

			// 仅对非重载自java.lang.Object的公共方法使用方法代理
			this.methodProxy = (Modifier.isPublic(method.getModifiers()) &&
					method.getDeclaringClass() != Object.class && !AopUtils.isEqualsMethod(method) &&
					!AopUtils.isHashCodeMethod(method) && !AopUtils.isToStringMethod(method) ?
					methodProxy : null);
		}

		@Override
		public Object proceed() throws Throwable {
			try {
				return super.proceed();
			} catch (Exception ex) {
				throw ex;
			} 
		}

		@Override
		protected Object invokeJoinpoint() throws Throwable {
			if (this.methodProxy != null) {
				return this.methodProxy.invoke(this.target, this.arguments);
			} else {
				return super.invokeJoinpoint();
			}
		}
	}
	
	/**
	 * 方法拦截器，用于没有通知链的静态目标。调用直接传递回目标。 
	 * 当代理需要被公开，并且无法确定该方法不会返回此值时使用
	 */
	private static class StaticUnadvisedInterceptor implements MethodInterceptor, Serializable {

		private final Object target;

		/**
		 * 方法拦截器，用于没有通知链的静态目标。调用直接传递回目标。 
		 * 当代理需要被公开，并且无法确定该方法不会返回此值时使用
		 */
		public StaticUnadvisedInterceptor(Object target) {
			this.target = target;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object retVal = methodProxy.invoke(this.target, args);
			return processReturnType(proxy, this.target, method, retVal);
		}
	}

	/** 静态目标的分派器 */
	private static class StaticDispatcher implements Dispatcher, Serializable {

		private final Object target;

		public StaticDispatcher(Object target) {
			this.target = target;
		}

		@Override
		public Object loadObject() {
			return this.target;
		}
	}

	/** {@code equals} 方法的调度处理 */
	private static class EqualsInterceptor implements MethodInterceptor, Serializable {

		private final AdvisedSupport advised;

		public EqualsInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
			Object other = args[0];
			if (proxy == other) {
				return true;
			}
			if (other instanceof Factory) {
				Callback callback = ((Factory) other).getCallback(INVOKE_EQUALS);
				if (!(callback instanceof EqualsInterceptor)) {
					return false;
				}
				AdvisedSupport otherAdvised = ((EqualsInterceptor) callback).advised;
				return AopProxyUtils.equalsInProxy(this.advised, otherAdvised);
			} else {
				return false;
			}
		}
	}

	/** {@code hashCode} 方法的调度处理 */
	private static class HashCodeInterceptor implements MethodInterceptor, Serializable {

		private final AdvisedSupport advised;

		public HashCodeInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
			return CglibAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
		}
	}
	
	/** 对应final方法，不做增强 */
	public static class SerializableNoOp implements NoOp, Serializable {}
	

	/** 调度在建议类上声明的任何方法 */
	private static class AdvisedDispatcher implements Dispatcher, Serializable {

		private final AdvisedSupport advised;

		public AdvisedDispatcher(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object loadObject() {
			return this.advised;
		}
	}
	
	/** 根据不同的方法分派对应处理的Callback对象 */
	class ProxyCallbackFilter implements CallbackFilter {
		private final AdvisedSupport advised;
		
		public ProxyCallbackFilter(AdvisedSupport advised) {
			super();
			this.advised = advised;
		}
		
		@Override
		public int accept(Method method) {
			if (AopUtils.isFinalizeMethod(method)) {
				logger.trace("Found finalize() method - using NO_OVERRIDE");
				DebugUtils.logFromAop(logger, "SerializableNoOp类处理的Object类声明方法：finalize()");
				return NO_OVERRIDE;
			}
			if (!this.advised.isOpaque() && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				DebugUtils.logFromAop(logger, "AdvisedDispatcher类处理的代理方法：" + method.toString());
				return DISPATCH_ADVISED;
			}
			if (AopUtils.isEqualsMethod(method)) {
				DebugUtils.logFromAop(logger, "EqualsInterceptor类处理的Object类声明方法：equals(Object)");
				return INVOKE_EQUALS;
			}
			if (AopUtils.isHashCodeMethod(method)) {
				DebugUtils.logFromAop(logger, "HashCodeInterceptor类处理的Object类声明方法：hashCode()");
				return INVOKE_HASHCODE;
			}
			
			Class<?> targetClass = this.advised.getTargetClass();
			List<?> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
			boolean haveAdvice = !chain.isEmpty();
			boolean exposeProxy = this.advised.isExposeProxy();
			boolean isStatic = this.advised.getTargetSource().isStatic();
			
			if (haveAdvice || exposeProxy) {
				DebugUtils.logFromAop(logger, "DynamicAdvisedInterceptor类处理的代理方法：" + method.toString());
				return AOP_PROXY;
			} else {
				if (exposeProxy || !isStatic) {
					DebugUtils.logFromAop(logger, "StaticUnadvisedInterceptor类处理的代理方法：" + method.toString());
					return INVOKE_TARGET;
				}
				Class<?> returnType = method.getReturnType();
				if (targetClass != null && returnType.isAssignableFrom(targetClass)) {
					DebugUtils.log(logger, "StaticUnadvisedInterceptor类处理的代理方法[返回它自身]："+method.toString());
					return INVOKE_TARGET;
				} else {
					DebugUtils.log(logger, "StaticDispatcher类处理的代理方法[不返回它自身]："+method.toString());
					return DISPATCH_TARGET;
				}
			}
		}
		
	}
	
	
}

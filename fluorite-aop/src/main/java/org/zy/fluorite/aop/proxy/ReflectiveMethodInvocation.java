package org.zy.fluorite.aop.proxy;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.ProxyMethodInvocation;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;
import org.zy.fluorite.aop.utils.AopProxyUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年7月11日 下午4:57:04;
 * @author zy(azurite-Y);
 * @Description
 */
public class ReflectiveMethodInvocation implements ProxyMethodInvocation, Cloneable{
	protected final Object proxy;

	protected final Object target;

	protected final Method method;

	protected Object[] arguments;

	private final Class<?> targetClass;

	/** 延迟初始化此调用的用户特定属性的映射 */
	private Map<String, Object> userAttributes;

	protected final List<?> interceptors;

	/** 当前调用的拦截器索引，从0开始 */
	private int currentInterceptorIndex = -1;
	
	
	/**
	 * 创建MethodInvocation实例
	 * @param proxy - 代理类对象
	 * @param target - 被代理类对象
	 * @param method - 代理方法
	 * @param arguments - 代理方法参数集
	 * @param targetClass - 被代理类Class对象
	 * @param interceptors - 对于此代理方法需调用的Advice链
	 */
	protected ReflectiveMethodInvocation( Object proxy, Object target, Method method, Object[] arguments,
			Class<?> targetClass, List<Object> interceptors) {
		this.proxy = proxy;
		this.target = target;
		this.targetClass = targetClass;
		// spring在此进行了桥接方法的过滤，by：BridgeMethodResolver【通过判断方法名、参数的个数以及泛型类型参数来确定原始方法】
		this.method = method;
		this.arguments = AopProxyUtils.adaptArgumentsIfNecessary(method, arguments);
		this.interceptors = interceptors;
	}
		
	@Override
	public Object proceed() throws Throwable {
		if (this.currentInterceptorIndex == this.interceptors.size() - 1) {
			return invokeJoinpoint();
		}
		Object interceptor = this.interceptors.get(++this.currentInterceptorIndex);
		return ((MethodInterceptor) interceptor).invoke(this);
	}	
	
	@Override
	public final Object getProxy() {
		return this.proxy;
	}

	@Override
	public final Object getThis() {
		return this.target;
	}

	@Override
	public final AccessibleObject getStaticPart() {
		return this.method;
	}

	/**
	 * 返回在代理接口上调用的方法。
	 * 可能与该接口的底层实现上调用的方法对应，也可能不对应
	 */
	@Override
	public final Method getMethod() {
		return this.method;
	}

	@Override
	public final Object[] getArguments() {
		return this.arguments;
	}

	@Override
	public void setArguments(Object... arguments) {
		this.arguments = arguments;
	}
	
	/** 使用反射调用连接点。子类可以覆盖此内容以使用自定义调用	 */
	protected Object invokeJoinpoint() throws Throwable {
		return ReflectionUtils.invokeMethod(this.target, this.method, this.arguments);
	}

	/**
	 * 返回这个调用对象的浅拷贝，包括原始参数数组的独立拷贝
	 */
	@Override
	public MethodInvocation invocableClone() {
		Object[] cloneArguments = this.arguments;
		if (this.arguments.length > 0) {
			// 构建参数数组的独立副本。
			cloneArguments = this.arguments.clone();
		}
		return invocableClone(cloneArguments);
	}

	@Override
	public MethodInvocation invocableClone(Object... arguments) {
		// 强制初始化用户属性映射，用于在克隆中拥有一个共享映射引用.
		if (this.userAttributes == null) {
			this.userAttributes = new HashMap<>();
		}

		try {
			ReflectiveMethodInvocation clone = (ReflectiveMethodInvocation) clone();
			clone.arguments = arguments;
			return clone;
		} catch (CloneNotSupportedException ex) {
			throw new IllegalStateException("无法克隆的对象类型 [" + getClass() + "]: " + ex);
		}
	}

	@Override
	public void setUserAttribute(String key, Object value) {
		if (value != null) {
			if (this.userAttributes == null) {
				this.userAttributes = new HashMap<>();
			}
			this.userAttributes.put(key, value);
		}
		else {
			if (this.userAttributes != null) {
				this.userAttributes.remove(key);
			}
		}
	}

	@Override
	public Object getUserAttribute(String key) {
		return (this.userAttributes != null ? this.userAttributes.get(key) : null);
	}

	/**
	 * 返回与此调用关联的用户属性。此方法提供了ThreadLocal的调用绑定替代方法。
	 * 这个映射是惰性地初始化的，不会在AOP框架本身中使用。
	 */
	public Map<String, Object> getUserAttributes() {
		if (this.userAttributes == null) {
			this.userAttributes = new HashMap<>();
		}
		return this.userAttributes;
	}

	@Override
	public String toString() {
		// 不要调用目标对象上的toString方法，它可能被代理。
		StringBuilder sb = new StringBuilder("ReflectiveMethodInvocation: ");
		sb.append(this.method).append("; ");
		if (this.target == null) {
			sb.append("target is null");
		} else {
			sb.append("target is of class [").append(this.target.getClass().getName()).append(']');
		}
		return sb.toString();
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}
}

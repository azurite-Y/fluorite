package org.zy.fluorite.beans.support;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午8:17:20;
 * @Description 表示在同一IOC上下文中查找对象的方法的重写。符合查找重写条件的方法不能有参数
 */
public class LookupOverride extends MethodOverride {
	private final String beanName;
	private Method method;
	
	protected LookupOverride(String methodName, String beanName) {
		super(methodName);
		this.beanName = beanName;
	}
	public LookupOverride(Method method, String beanName) {
		super(method.getName());
		this.beanName = beanName;
		this.method = method;
	}

	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public String getBeanName() {
		return beanName;
	}
	
	@Override
	public boolean matches(Method method) {
		if (this.method != null) {
			return method.equals(this.method);
		}
		else {
			return (method.getName().equals(getMethodName()) && (!isOverloaded() ||
					Modifier.isAbstract(method.getModifiers()) || method.getParameterCount() == 0));
		}
	}

	@Override
	public String toString() {
		return "LookupOverride for method '" + getMethodName() + "'";
	}
}

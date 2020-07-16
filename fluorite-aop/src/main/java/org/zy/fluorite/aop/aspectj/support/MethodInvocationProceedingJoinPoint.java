package org.zy.fluorite.aop.aspectj.support;

import java.lang.reflect.Method;

import org.zy.fluorite.aop.aspectj.interfaces.ProceedingJoinPoint;
import org.zy.fluorite.aop.interfaces.ProxyMethodInvocation;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月12日 下午4:56:29;
 * @author zy(azurite-Y);
 * @Description
 */
public class MethodInvocationProceedingJoinPoint implements ProceedingJoinPoint{

	private final ProxyMethodInvocation methodInvocation;

	private Object[] args;
	
	public MethodInvocationProceedingJoinPoint(ProxyMethodInvocation methodInvocation) {
		Assert.notNull(methodInvocation, "MethodInvocation不能为null");
		this.methodInvocation = methodInvocation;
	}
	
	@Override
	public Object getThis() {
		return this.methodInvocation.getProxy();
	}

	@Override
	public Object getTarget() {
		return this.methodInvocation.getThis();
	}

	@Override
	public Object[] getArgs() {
		if (this.args == null) {
			this.args = this.methodInvocation.getArguments().clone();
		}
		return this.args;
	}

	@Override
	public Method getJoinPointMethod() {
		return this.methodInvocation.getMethod();
	}

	@Override
	public Object proceed() throws Throwable {
		return this.methodInvocation.invocableClone().proceed();
	}

	@Override
	public Object proceed(Object[] arguments) throws Throwable {
		Assert.notNull(arguments, "传递给下一个调用的参数数组不能为null");
		if (arguments.length != this.methodInvocation.getArguments().length) {
			throw new IllegalArgumentException("参数个数不匹配，期望的参数个数：" +
					this.methodInvocation.getArguments().length + "，传递而来的参数个数：" +arguments.length);
		}
		this.methodInvocation.setArguments(arguments);
		return this.methodInvocation.invocableClone(args).proceed();
	}

}

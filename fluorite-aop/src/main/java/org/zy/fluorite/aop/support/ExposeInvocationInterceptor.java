package org.zy.fluorite.aop.support;

import java.io.Serializable;

import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;
import org.zy.fluorite.core.interfaces.PriorityOrdered;
import org.zy.fluorite.core.subject.NamedThreadLocal;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月8日 下午2:18:50;
 * @author zy(azurite-Y);
 * @Description 存储当前线程调用的MethodInvocation，保证其调用的线程安全
 */
@SuppressWarnings("serial")
public class ExposeInvocationInterceptor implements MethodInterceptor, PriorityOrdered, Serializable {
	
	public static final ExposeInvocationInterceptor INSTANCE = new ExposeInvocationInterceptor();

	private ExposeInvocationInterceptor() {}

	private static final ThreadLocal<MethodInvocation> invocation = new NamedThreadLocal<>(
			"当前正在调用的Advice方法");

	/**
	 * 返回存储在当前线程ThreadLocal中的MethodInvocation对象
	 * @throws IllegalStateException - 如果没有正在进行的AOP调用，或者ExposeInvocationInterceptor未添加到此侦听器链
	 */
	public static MethodInvocation currentInvocation() throws IllegalStateException {
		MethodInvocation mi = invocation.get();
		Assert.notNull(mi,"找不到MethodInvocation:请检查AOP调用是否正在进行，以及ExposeInvocationInterceptor是否在侦听器链的前端。"
				+ "特别要注意，具有最高优先级的通知将在ExposeInvocationInterceptor之前执行！"
				+ "此外，ExposeInvocationInterceptor和ExposeInLocationInterceptor.currentInvocation（）必须从同一线程调用");
		return mi;
	}

	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		MethodInvocation oldInvocation = invocation.get();
		invocation.set(mi);
		try {
			return mi.proceed();
		} finally {
			invocation.set(oldInvocation);
		}
	}

	@Override
	public int getOrder() {
		return PriorityOrdered.HIGHEST_PRECEDENCE + 1;
	}

	private Object readResolve() {
		return INSTANCE;
	}

	public static final Advisor ADVISOR = new DefaultPointcutAdvisor(INSTANCE) {
		@Override
		public String toString() {
			return ExposeInvocationInterceptor.class.getName() + ".ADVISOR";
		}
	};
}

package org.zy.fluorite.aop.interfaces.function;

import org.zy.fluorite.aop.interfaces.Interceptor;
import org.zy.fluorite.aop.interfaces.MethodInvocation;

/**
 * @DateTime 2020年7月5日 上午8:22:45;
 * @author zy(azurite-Y);
 * @Description 在调用目标的过程中拦截对目标的调用。嵌套在目标的“顶部”
 */
@FunctionalInterface
public interface MethodInterceptor extends Interceptor {

	/**
	 * 实现此方法以在调用前后执行额外的处理
	 * @param invocation - 方法调用连接点
	 * @throws Throwable - 如果拦截器或目标对象抛出异常
	 */
	Object invoke(MethodInvocation invocation) throws Throwable;

}

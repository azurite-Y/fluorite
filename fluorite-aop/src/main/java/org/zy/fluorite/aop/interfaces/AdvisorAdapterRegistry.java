package org.zy.fluorite.aop.interfaces;

import java.util.List;

import org.zy.fluorite.aop.exception.UnknownAdviceTypeException;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;

/**
 * @DateTime 2020年7月4日 下午6:29:34;
 * @author zy(azurite-Y);
 * @Description Advisor适配器注册表的接口
 */
public interface AdvisorAdapterRegistry {
	/**
	 * 包装advice返回Advisor，如果advice参数的类型是Advisor那么就按原样返回
	 * <p>默认情况下至少应支持MethodIntercepto、MethodBeforeAdvice、AfterReturningAdvice、ThrowsAdvice
	 * @throws UnknownAdviceTypeException 如果没有注册的advisor适配器可以包装假定的advice则抛出此异常
	 */
	Advisor wrap(Object advice) throws UnknownAdviceTypeException;

	/**
	 * 返回一个AOP方法拦截器数组，以允许在基于拦截的框架中使用给定的Advisor。
	 * 不用担心与Advisor相关的切入点，如果它是PointcutAdvisor则只返回一个拦截器。
	 * @param advisor 寻找拦截器的Advisor
	 */
	List<MethodInterceptor> getInterceptors(Advisor advisor) throws UnknownAdviceTypeException;

	/**
	 * 注册给定的AdvisorAdapter。请注意，对于AOP拦截器或Advices，
	 * 不需要注册适配器：这些必须由AdvisorAdapterRegistry实现自动识别。
	 */
	void registerAdvisorAdapter(AdvisorAdapter adapter);
}

package org.zy.fluorite.aop.interfaces;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @DateTime 2020年7月4日 下午4:18:40;
 * @author zy(azurite-Y);
 * @Description advisor链的工厂接口
 */
public interface AdvisorChainFactory {
	/**
	 * 确定给定advisor链配置的MethodInterceptor对象列表
	 * @param config
	 * @param method - 代理对象
	 * @param targetClass - 目标类（可以为null，表示没有目标对象的代理，在这种情况下，方法的声明类是次佳选项）
	 * @return MethodInterceptor列表（可能还包括Interceptor和DynamicMethodMatchers）
	 */
	List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method, Class<?> targetClass);

}

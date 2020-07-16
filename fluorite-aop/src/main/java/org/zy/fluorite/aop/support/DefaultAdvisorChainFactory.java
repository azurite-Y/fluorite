package org.zy.fluorite.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.aop.interfaces.Advised;
import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.aop.interfaces.AdvisorAdapterRegistry;
import org.zy.fluorite.aop.interfaces.AdvisorChainFactory;
import org.zy.fluorite.aop.interfaces.IntroductionAdvisor;
import org.zy.fluorite.aop.interfaces.Pointcut;
import org.zy.fluorite.aop.interfaces.PointcutAdvisor;
import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;
import org.zy.fluorite.aop.support.adapter.GlobalAdvisorAdapterRegistry;

/**
 * @DateTime 2020年7月4日 下午4:22:33;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class DefaultAdvisorChainFactory implements AdvisorChainFactory, Serializable {

	@Override
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config , Method method , Class<?> targetClass) {
		AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();
		Advisor[] advisors = config.getAdvisors();
		List<Object> interceptorList = new ArrayList<>(advisors.length);
		Class<?> actualClass = (targetClass != null ? targetClass : method.getDeclaringClass());
		for (Advisor advisor : advisors) {
			if (advisor instanceof PointcutAdvisor) {
				Pointcut pointcut = ((PointcutAdvisor) advisor).getPointcut();
				// 若已经过预筛选则就不调用matcher(Class<?>)方法
				if ( (config.isPreFiltered() || pointcut.matcher(targetClass)) &&  pointcut.matcher(targetClass, method) ) {
					// 将给定的Advisor实现转换或包装为MethodInterceptor
					List<MethodInterceptor> interceptors = registry.getInterceptors(advisor);
					interceptorList.addAll(interceptors);
				}
			} else if (advisor instanceof IntroductionAdvisor) {
				IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
				if (config.isPreFiltered() || ia.getClassFilter().matches(actualClass)) {
					/* 将给定的Advisor实现转换或包装为MethodInterceptor */
					List<MethodInterceptor> interceptors = registry.getInterceptors(advisor);
					interceptorList.addAll(interceptors);
				}
			}
		}

		return interceptorList;
	}
}

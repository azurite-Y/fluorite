package org.zy.fluorite.aop.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.aop.interfaces.FluoriteProxy;
import org.zy.fluorite.aop.interfaces.IntroductionAdvisor;
import org.zy.fluorite.aop.interfaces.Pointcut;
import org.zy.fluorite.aop.interfaces.PointcutAdvisor;
import org.zy.fluorite.aop.interfaces.TargetClassAware;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;

/**
 * @DateTime 2020年7月5日 上午10:00:11;
 * @author zy(azurite-Y);
 * @Description Joinpoint
 */
public final class AopUtils {
	
	/**
	 * 确定给定bean实例的目标类，它可能是一个AOP代理。
	 * 返回AOP代理的目标类，否则返回普通类。
	 * @param candidate
	 * @return
	 */
	public static Class<?> getTargetClass(Object candidate) {
		Assert.notNull(candidate, "Candidate object must not be null");
		Class<?> result = null;
		if (candidate instanceof TargetClassAware) {
			result = ((TargetClassAware) candidate).getTargetClass();
		}
		if (result == null) {
			result = (isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
		}
		return result;
	}
	
	/**
	 * 确定适用于给定类的候选Advisor列表的子列表
	 * @param candidateAdvisors - 评估的Advisor
	 * @param beanClass - 当前Bean实例的Class对象，可能是切点Bean
	 * @return 可以应用于给定类的对象的Advisor子列表（可以是传入列表）
	 */
	public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> beanClass) {
		if (candidateAdvisors.isEmpty()) {
			return candidateAdvisors;
		}
		List<Advisor> eligibleAdvisors = new ArrayList<>();
		for (Advisor candidate : candidateAdvisors) {
			if (candidate instanceof IntroductionAdvisor) {
				if (canApply(candidate, beanClass)) {
					eligibleAdvisors.add(candidate);
				}
				// 已处理
				continue;
			} else if ( canApply(candidate, beanClass) ) {
				eligibleAdvisors.add(candidate);
			}
		}
		return eligibleAdvisors;
	}

	/**
	 * 指定的Advisor是否适用于当前Bean
	 * 
	 * @param advisor
	 * @param targetClass  - 当前Bean的Class对象
	 * @return
	 */
	public static boolean canApply(Advisor advisor, Class<?> targetClass) {
		if (advisor instanceof IntroductionAdvisor) {
			return ((IntroductionAdvisor) advisor).getClassFilter().matches(targetClass);
		} else if (advisor instanceof PointcutAdvisor) {
			PointcutAdvisor pca = (PointcutAdvisor) advisor;
			return canApply(pca.getPointcut(), targetClass);
		} else {
			// 它没有切点，所以假设它适用
			return true;
		}
	}

	/**
	 * 指定的切点能否应用于给定类
	 * @param pointcut - 要检查的静态或动态切点
	 * @param targetClass - 当前Bean的Class对象
	 * @return
	 */
	public static boolean canApply(Pointcut pointcut, Class<?> targetClass) {
		Assert.notNull(pointcut, "Pointcut不能为null");
		
		Set<Class<?>> classes = new LinkedHashSet<>();
		if (!Proxy.isProxyClass(targetClass)) {
			classes.add(ClassUtils.getUserClass(targetClass));
		}
		classes.addAll(ClassUtils.getAllInterfacesToSet(targetClass));
		
		Method[] declaredMethods = null;
		for (Class<?> clz : classes) {
			if (!pointcut.matcher(clz)) {
				return false;
			}
			declaredMethods = clz.getDeclaredMethods();
			for (Method method : declaredMethods) {
				if (pointcut.matcher(clz, method)) {
					return true;
				}
			}
		}
		
		return false;
	}

	/** 判断给定方法是否是 {@linkplain Object#finalize()} 方法*/
	public static boolean isFinalizeMethod(Method method) {
		return (method != null && method.getName().equals("finalize") && method.getParameterCount() == 0);
	}

	/** 判断给定方法是否是 {@linkplain Object#equals(Object) } 方法*/
	public static boolean isEqualsMethod(Method method) {
		if (method == null || !method.getName().equals("equals")) {
			return false;
		}
		if (method.getParameterCount() != 1) {
			return false;
		}
		return method.getParameterTypes()[0] == Object.class;
	}

	/** 判断给定方法是否是 {@linkplain Object#hashCode() } 方法*/
	public static boolean isHashCodeMethod(Method method) {
		return (method != null && method.getName().equals("hashCode") && method.getParameterCount() == 0);
	}

	/** 判断给定方法是否是 {@linkplain Object#toString() } 方法*/
	public static boolean isToStringMethod(Method method) {
		return (method != null && method.getName().equals("toString") && method.getParameterCount() == 0);
	}

	/**
	 * 判断指定对象是否由Cglib创建
	 * @param object
	 * @return
	 */
	public static boolean isCglibProxy(Object object) {
		return (object instanceof FluoriteProxy &&
				object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR));
	}

}

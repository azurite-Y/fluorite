package org.zy.fluorite.aop.aspectj.interfaces;

import java.lang.reflect.Method;
import java.util.List;

import org.zy.fluorite.aop.aspectj.support.AspectJPluralisticPointcut;
import org.zy.fluorite.aop.exception.AopConfigException;
import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;

/**
 * @DateTime 2020年7月5日 下午3:08:12;
 * @author zy(azurite-Y);
 * @Description 
 */
public interface AspectJAdvisorFactory {
	
	/** 判断假定的切面类是否真的是一个切面 */
	boolean isAspect(AnnotationMetadata metadata);

	/**
	 * 给定的类是否是有效的切面类
	 * @param aspectClass - 假设要验证的切面注解类
	 * @throws AopConfigException - 如果类是无效的切面类
	 * @throws NotAnAtAspectException - 如果类根本不是一个切面
	 */
	void validate(Class<?> aspectClass) throws AopConfigException;

	/**
	 * 在指定的切面实例工厂中构建Advisor
	 * @param aspectInstanceFactory - 切面实例工厂
	 * @return
	 */
	List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory);

	/**
	 * 为给定的Advice方法构建一个Advisor
	 * @param candidateAdviceMethod 候选的Advice方法
	 * @param aspectInstanceFactory  - 切面工厂
	 * @param declarationOrder 切面中的声明顺序
	 * @param aspectName - 切面名称
	 */
	Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory,
			int declarationOrder, String aspectName);

	/**
	 * 为给定的Advice方法构建一个Advisor
	 * @param candidateAdviceMethod 候选的Advice方法
	 * @param expressionPointcut  - AspectJ 切点表达式
	 * @param declarationOrder 切面中的声明顺序
	 * @param aspectName - 切面名称
	 */
	Advice getAdvice(Method candidateAdviceMethod, AspectJPluralisticPointcut expressionPointcut,
			MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName);

}

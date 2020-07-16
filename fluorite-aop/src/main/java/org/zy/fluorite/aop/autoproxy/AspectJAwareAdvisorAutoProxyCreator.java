package org.zy.fluorite.aop.autoproxy;

import java.util.List;

import org.zy.fluorite.aop.aspectj.advice.AbstractAspectJAdvice;
import org.zy.fluorite.aop.aspectj.interfaces.AspectJPrecedenceInformation;
import org.zy.fluorite.aop.aspectj.interfaces.InstantiationModelAwarePointcutAdvisor;
import org.zy.fluorite.aop.aspectj.support.AspectJPluralisticPointcut;
import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.aop.interfaces.PointcutAdvisor;
import org.zy.fluorite.aop.support.AspectJPointcutAdvisor;
import org.zy.fluorite.aop.support.ExposeInvocationInterceptor;
import org.zy.fluorite.beans.support.AnnotationAwareOrderComparator;

/**
 * @DateTime 2020年7月5日 下午1:56:49;
 * @author zy(azurite-Y);
 * @Description AbstractAdvisorAutoProxyCreator子类，公开AspectJ的调用上下文
 */
@SuppressWarnings({ "serial"})
public class AspectJAwareAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator  {
	
	@Override
	protected void extendAdvisors(List<Advisor> candidateAdvisors) {
		if (!candidateAdvisors.isEmpty()) {
			boolean foundAspectJAdvice = false;
			for (Advisor advisor : candidateAdvisors) {
				if (isAspectJAdvice(advisor)) {
					foundAspectJAdvice = true;
					break;
				}
			}
			if (foundAspectJAdvice && !candidateAdvisors.contains(ExposeInvocationInterceptor.ADVISOR)) {
				candidateAdvisors.add(0, ExposeInvocationInterceptor.ADVISOR);
			}
		}
	}
	
	/** 确定给定的Advisor 是否包含切面Advice。 */
	private boolean isAspectJAdvice(Advisor advisor) {
		return (advisor instanceof InstantiationModelAwarePointcutAdvisor || advisor.getAdvice() instanceof AbstractAspectJAdvice ||
				(advisor instanceof PointcutAdvisor && ((PointcutAdvisor) advisor).getPointcut() instanceof AspectJPluralisticPointcut));
	}

	/** 若两Advisor是同一类型的通知则根据@Order注解排序，反之则保持原来的顺序 */
	@Override
	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		advisors.sort( (a1,a2) -> {
			if ( a2.getAdvice().getClass().equals(a1.getAdvice().getClass()) ) {
				if (a1 instanceof AspectJPrecedenceInformation &&  a2 instanceof AspectJPrecedenceInformation) {
					AspectJPrecedenceInformation a1Convert = ((AspectJPrecedenceInformation)a1);
					AspectJPrecedenceInformation a2Convert = ((AspectJPrecedenceInformation)a2);
					return AnnotationAwareOrderComparator.INSTANCE.compare(
							a1Convert.getAspectJAdviceMethod() , a2Convert.getAspectJAdviceMethod());
				}
			}
			return 0;
		});
		return advisors;
	}
	
	@Override
	protected boolean shouldSkip(Class<?> beanClass, String beanName) {
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		for (Advisor advisor : candidateAdvisors) {
			// 若当前Bean是切面类则跳过
			if (advisor instanceof AspectJPointcutAdvisor && ((AspectJPointcutAdvisor) advisor).getAspectName().equals(beanName)) {
				return true;
			}
		}
		return super.shouldSkip(beanClass, beanName);
	}
}

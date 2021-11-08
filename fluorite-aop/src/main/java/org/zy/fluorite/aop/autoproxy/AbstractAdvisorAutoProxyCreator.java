package org.zy.fluorite.aop.autoproxy;

import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.aop.interfaces.TargetSource;
import org.zy.fluorite.aop.utils.AopUtils;
import org.zy.fluorite.beans.factory.exception.BeanCreationException;
import org.zy.fluorite.beans.support.AnnotationAwareOrderComparator;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @DateTime 2020年7月5日 上午9:17:43;
 * @author zy(azurite-Y);
 * @Description 通用自动代理创建器，根据每个bean检测到的顾问为特定bean构建AOP代理。
 * <p>子类可以重写findCandidateAdvisors()方法来返回应用于任何对象的顾问的自定义列表。
 * 子类还可以重写继承的shouldSkip方法以从自动代理中排除certainobjects。</p>
 * <p>Advisor或Advice需要排序可以实现{@link Ordered} 接口或标注@Order注解，
 * 而未实现此接口或未标注的则将被会被放到排序链的末端</p>
 */
@SuppressWarnings("serial")
public class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {
	
	private volatile String[] cachedAdvisorBeanNames;
	
	/**
	 * 查找所有符合条件的Advisor自动代理此类
	 * @param beanClass
	 * @param beanName
	 * @return 如果没有切入点或拦截器，则返回空集，而不是null
	 */
	protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
		// 找到属于自动代理候选
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		// 找到当前切点所需的切面
		List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
		// 设置Advisor执行链顶部的Advisor实现 - ExposeInvocationInterceptor
		extendAdvisors(eligibleAdvisors);
		if (!eligibleAdvisors.isEmpty()) {
			// Advise排序，支持@Order和@Priority（若两个相同类型的Advice其中标注了@Priority注解的优先级高）
			eligibleAdvisors = sortAdvisors(eligibleAdvisors);
		}
		return eligibleAdvisors;
	}

	/**
	 * 根据Order对顾问进行分类。子类可以选择重写此方法以自定义排序策略
	 * <p>Advise排序，支持@Order和@Priority（若两个相同的Advice其中标注了@Priority注解的优先级高）</p>
	 * @param advisors
	 * @return
	 */
	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		AnnotationAwareOrderComparator.sort(advisors);
		return advisors;
	}

	/**
	 * 搜索给定的候选Advisor以找到可以应用于指定bean的所有Advisor
	 * @param candidateAdvisors
	 * @param beanClass
	 * @param beanName
	 * @return 适用的Advisor名单
	 */
	protected List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> beanClass,String beanName) {
		ProxyCreationContext.setCurrentProxiedBeanName(beanName);
		try {
			return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
		} finally {
			ProxyCreationContext.setCurrentProxiedBeanName(null);
		}
	}
	
	protected List<Advisor> findCandidateAdvisors() {
		// 确定advisor bean名称的列表（如果尚未缓存）
		String[] advisorNames = this.cachedAdvisorBeanNames;
		if (advisorNames == null) {
			advisorNames = beanFactory.getBeanNamesForType(Advisor.class, true, false);
			this.cachedAdvisorBeanNames = advisorNames;
		}
		if (advisorNames.length == 0) {
			return new ArrayList<>();
		}

		List<Advisor> advisors = new ArrayList<>();
		for (String name : advisorNames) {
			if (isEligibleBean(name)) {
				if (this.beanFactory.isCurrentlyInCreation(name)) {
//					DebugUtils.logFromAop(logger, "跳过正在创建的Advisor：" + name);
				} else {
					try {
						advisors.add(this.beanFactory.getBean(name, Advisor.class));
					} catch (BeanCreationException ex) {
						throw ex;
					}
				}
			}
		}
		return advisors;
	}

	/**
	 * 确定具有给定名称的切面bean是否合格。默认实现始终返回true。
	 * @param name
	 * @return
	 */
	private boolean isEligibleBean(String name) {
		return true;
	}

	/** 此自动代理创建者始终返回预先筛选的顾问 */
	@Override
	protected boolean advisorsPreFiltered() {
		return true;
	}
	
	@Override
	protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass , String beanName , TargetSource customTargetSource) throws BeansException {
		List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
		if (advisors.isEmpty()) {
			return null;
		}
		return advisors.toArray();
	}

	/** 子类可以重写以注册其他顾问的扩展钩子，给定迄今为止获得的排序顾问。默认实现为空 */
	protected void extendAdvisors(List<Advisor> candidateAdvisors) {}

	
}

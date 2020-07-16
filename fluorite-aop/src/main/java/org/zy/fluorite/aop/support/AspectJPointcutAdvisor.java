package org.zy.fluorite.aop.support;

import org.zy.fluorite.aop.aspectj.advice.AbstractAspectJAdvice;
import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.Pointcut;
import org.zy.fluorite.aop.interfaces.PointcutAdvisor;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月7日 下午1:39:57;
 * @author zy(azurite-Y);
 * @Description 将AbstractAspectJadvice应用于PointcutAdvisor接口
 */
public class AspectJPointcutAdvisor implements PointcutAdvisor, Ordered {
	private final AbstractAspectJAdvice advice;

	private final Pointcut pointcut;

	private Integer order;


	/** 根据给定的advice创建一个新的AspectJPointcutAdvisor */
	public AspectJPointcutAdvisor(AbstractAspectJAdvice advice) {
		Assert.notNull(advice, "Advice must not be null");
		this.advice = advice;
		this.pointcut = advice.buildSafePointcut();
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		if (this.order != null) {
			return this.order;
		}
		else {
			return this.advice.getOrder();
		}
	}

	@Override
	public Advice getAdvice() {
		return this.advice;
	}

	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

	public String getAspectName() {
		return this.advice.getAspectName();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AspectJPointcutAdvisor)) {
			return false;
		}
		AspectJPointcutAdvisor otherAdvisor = (AspectJPointcutAdvisor) other;
		return this.advice.equals(otherAdvisor.advice);
	}

	@Override
	public int hashCode() {
		return AspectJPointcutAdvisor.class.hashCode() * 29 + this.advice.hashCode();
	}
}

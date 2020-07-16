package org.zy.fluorite.aop.support;

import java.io.Serializable;

import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.PointcutAdvisor;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.utils.ObjectUtils;

/**
 * @DateTime 2020年7月7日 上午12:26:39;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public abstract class AbstractPointcutAdvisor implements PointcutAdvisor, Ordered, Serializable {
	private Integer order;


	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		if (this.order != null) {
			return this.order;
		}
		Advice advice = getAdvice();
		if (advice instanceof Ordered) {
			return ((Ordered) advice).getOrder();
		}
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PointcutAdvisor)) {
			return false;
		}
		PointcutAdvisor otherAdvisor = (PointcutAdvisor) other;
		return (ObjectUtils.nullSafeEquals(getAdvice(), otherAdvisor.getAdvice()) &&
				ObjectUtils.nullSafeEquals(getPointcut(), otherAdvisor.getPointcut()));
	}

	@Override
	public int hashCode() {
		return PointcutAdvisor.class.hashCode();
	}
}

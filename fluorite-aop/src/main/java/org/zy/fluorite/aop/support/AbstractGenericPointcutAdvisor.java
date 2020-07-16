package org.zy.fluorite.aop.support;

import org.zy.fluorite.aop.interfaces.Advice;

/**
 * @DateTime 2020年7月7日 上午12:31:26;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public abstract class AbstractGenericPointcutAdvisor extends AbstractPointcutAdvisor {
	private Advice advice = EMPTY_ADVICE;

	/** 指定该advisor应该应用的Advice */
	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	@Override
	public Advice getAdvice() {
		return this.advice;
	}

	@Override
	public String toString() {
		return getClass().getName() + ": advice [" + getAdvice() + "]";
	}
}

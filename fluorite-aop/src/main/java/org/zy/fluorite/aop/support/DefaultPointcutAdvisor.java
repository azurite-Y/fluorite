package org.zy.fluorite.aop.support;

import java.io.Serializable;

import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.Pointcut;

/**
 * @DateTime 2020年7月7日 上午12:33:19;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class DefaultPointcutAdvisor extends AbstractGenericPointcutAdvisor implements Serializable {
	private Pointcut pointcut = Pointcut.TRUE;

	/** 
	 * 创建一个空的DefaultPointcutAdvisor。
	 * 通知必须在使用setter方法之前设置。Pointcut通常也会被设置，但是默认设置为Pointcut. true。
	 */
	public DefaultPointcutAdvisor() {}

	/**
	 * 创建一个与所有方法匹配的DefaultPointcutAdvisor。{@linkplain Pointcut.TRUE}将用作切入点
	 * @param advice - 要使用的Advice
	 */
	public DefaultPointcutAdvisor(Advice advice) {
		this(Pointcut.TRUE, advice);
	}

	/** 
	 * 创建一个DefaultPointcutAdvisor，指定切入点和通知
	 * @param pointcut - 以通知为目标的切入点
	 * @param advice - 当切入点匹配时运行的通知
	 */
	public DefaultPointcutAdvisor(Pointcut pointcut, Advice advice) {
		this.pointcut = pointcut;
		setAdvice(advice);
	}

	/** 指定以通知为目标的切入点。默认是Pointcut.TRUE。 */
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = (pointcut != null ? pointcut : Pointcut.TRUE);
	}

	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

	@Override
	public String toString() {
		return getClass().getName() + ": pointcut [" + getPointcut() + "]; advice [" + getAdvice() + "]";
	}
}

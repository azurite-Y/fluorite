package org.zy.fluorite.aop.support;

import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.beans.factory.aware.BeanFactoryAware;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2021年9月16日;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public abstract class AbstractBeanFactoryPointcutAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {
	private String adviceBeanName;

	private BeanFactory beanFactory;

	private transient volatile Advice advice;

	@Override
	public Advice getAdvice() {
		// advice：TransactionInterceptor
		Advice advice = this.advice;
		if (advice != null) {
			return advice;
		}

		Assert.notNull(this.adviceBeanName , "'adviceBeanName' 必须指定");
		Assert.notNull(this.beanFactory , "'beanFactory' 不能为null");

		// 从Bean工厂中获得advice对象
		advice = this.beanFactory.getBean(this.adviceBeanName, Advice.class);
		this.advice = advice;
		return advice;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public String getAdviceBeanName() {
		return this.adviceBeanName;
	}

	public void setAdviceBeanName(String adviceBeanName) {
		this.adviceBeanName = adviceBeanName;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append(": advice ");
		if (this.adviceBeanName != null) {
			sb.append("bean '").append(this.adviceBeanName).append("'");
		} else {
			sb.append(this.advice);
		}
		return sb.toString();
	}
}

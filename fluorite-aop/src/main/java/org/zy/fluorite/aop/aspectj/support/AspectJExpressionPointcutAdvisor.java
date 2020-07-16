package org.zy.fluorite.aop.aspectj.support;

import org.zy.fluorite.aop.interfaces.Pointcut;
import org.zy.fluorite.aop.support.AbstractGenericPointcutAdvisor;
import org.zy.fluorite.beans.factory.aware.BeanFactoryAware;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;

/**
 * @DateTime 2020年7月7日 上午12:38:16;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class AspectJExpressionPointcutAdvisor  extends AbstractGenericPointcutAdvisor implements BeanFactoryAware{
	private final AspectJPluralisticPointcut pointcut = new AspectJPluralisticPointcut();

	public String getExpression() {
		return this.pointcut.getExpression();
	}

	public void setParameterNames(String... names) {
		this.pointcut.setPointcutParameterNames(names);
	}

	public void setParameterTypes(Class<?>... types) {
		this.pointcut.setPointcutParameterTypes(types);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.pointcut.setBeanFactory(beanFactory);
	}

	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}
}

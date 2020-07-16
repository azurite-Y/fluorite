package org.zy.fluorite.aop.autoproxy;

import java.util.List;

import org.zy.fluorite.aop.aspectj.interfaces.AspectJAdvisorFactory;
import org.zy.fluorite.aop.aspectj.support.BeanFactoryAspectJAdvisorsBuilder;
import org.zy.fluorite.aop.aspectj.support.ReflectiveAspectJAdvisorFactory;
import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月5日 下午2:16:16;
 * @author zy(azurite-Y);
 * @Description AspectJAwareAdvisorAutoProxyCreator子类，用于处理当前应用程序上下文中的所有AspectJ注解和Advisor
 */
@SuppressWarnings("serial")
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {
	private AspectJAdvisorFactory aspectJAdvisorFactory;

	private BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;
	
	public AnnotationAwareAspectJAutoProxyCreator() {}
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		Assert.isTrue(beanFactory instanceof ConfigurableListableBeanFactory
				, "AdvisorAutoProxyCreator感知的BeanFactory对象必须实现ConfigurableListableBeanFactory接口，by："+beanFactory);
		super.setBeanFactory(beanFactory);
		if (aspectJAdvisorFactory == null) {
			aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory(beanFactory);
		}
		aspectJAdvisorsBuilder = new BeanFactoryAspectJAdvisorsBuilder((ConfigurableListableBeanFactory) beanFactory, aspectJAdvisorFactory);
	}
	
	@Override
	protected boolean isInfrastructureClass(Class<?> beanClass , AnnotationMetadata metadata) {
		return (super.isInfrastructureClass(beanClass , metadata) ||
				(this.aspectJAdvisorFactory != null && this.aspectJAdvisorFactory.isAspect(metadata)));
	}
	
	@Override
	protected List<Advisor> findCandidateAdvisors() {
		// 查找BeanFactory中的实现Advisor接口的Bean
		List<Advisor> advisors = super.findCandidateAdvisors();
		// 为bean工厂中的所有AspectJ切面构建Advisor
		if (this.aspectJAdvisorsBuilder != null) {
			advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
		}
		return advisors;
	}
	
	public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
		Assert.notNull(aspectJAdvisorFactory, "AspectJAdvisorFactory不能为null");
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
	}
	public AspectJAdvisorFactory getAspectJAdvisorFactory() {
		return aspectJAdvisorFactory;
	}
	public BeanFactoryAspectJAdvisorsBuilder getAspectJAdvisorsBuilder() {
		return aspectJAdvisorsBuilder;
	}
	
	@Override
	public boolean isProxyTargetClass() {
		return true;
	}
}

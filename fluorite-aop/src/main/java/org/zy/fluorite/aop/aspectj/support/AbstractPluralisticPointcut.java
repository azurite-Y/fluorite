package org.zy.fluorite.aop.aspectj.support;

import org.zy.fluorite.aop.aspectj.support.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.zy.fluorite.aop.interfaces.PluralisticPointcut;

/**
 * @DateTime 2020年7月9日 下午5:01:29;
 * @author zy(azurite-Y);
 * @Description 多元化的表达式切点抽象超类，提供多元化的属性操作方法实现
 */
public abstract class AbstractPluralisticPointcut implements PluralisticPointcut {
	protected AspectJAnnotation<?>  aspectJAnnotation;

	public AspectJAnnotation<?> getAspectJAnnotation() {
		return aspectJAnnotation;
	}

	public void setAspectJAnnotation(AspectJAnnotation<?> aspectJAnnotation) {
		this.aspectJAnnotation = aspectJAnnotation;
	}
}

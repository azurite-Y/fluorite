package org.zy.fluorite.aop.aspectj.support;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.zy.fluorite.aop.aspectj.interfaces.AspectJAdvisorFactory;
import org.zy.fluorite.aop.aspectj.interfaces.AspectJPrecedenceInformation;
import org.zy.fluorite.aop.aspectj.interfaces.InstantiationModelAwarePointcutAdvisor;
import org.zy.fluorite.aop.aspectj.interfaces.MetadataAwareAspectInstanceFactory;
import org.zy.fluorite.aop.aspectj.support.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.Pointcut;
import org.zy.fluorite.core.annotation.Lazy;
import org.zy.fluorite.core.exception.FluoriteRuntimeException;

/**
 * @DateTime 2020年7月6日 下午4:44:02;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class InstantiationModelAwarePointcutAdvisorImpl implements InstantiationModelAwarePointcutAdvisor, 
		AspectJPrecedenceInformation, Serializable {

	private final AspectJPluralisticPointcut declaredPointcut;

	private final Class<?> declaringClass;

	private final String methodName;

	private final Class<?>[] parameterTypes;

	private transient Method aspectJAdviceMethod;

	private final AspectJAdvisorFactory aspectJAdvisorFactory;

	private final MetadataAwareAspectInstanceFactory aspectInstanceFactory;

	private final int declarationOrder;

	private final String aspectName;

	private final Pointcut pointcut;

	private final boolean lazy;

	private Advice instantiatedAdvice;

	private Boolean isBeforeAdvice;

	private Boolean isAfterAdvice;

	public InstantiationModelAwarePointcutAdvisorImpl(AspectJPluralisticPointcut declaredPointcut,
			Method aspectJAdviceMethod, AspectJAdvisorFactory aspectJAdvisorFactory,
			MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {

		this.declaredPointcut = declaredPointcut;
		this.declaringClass = aspectJAdviceMethod.getDeclaringClass();
		this.methodName = aspectJAdviceMethod.getName();
		this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
		this.aspectJAdviceMethod = aspectJAdviceMethod;
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
		this.aspectInstanceFactory = aspectInstanceFactory;
		this.declarationOrder = declarationOrder;
		this.aspectName = aspectName;

		// 判断切面是否是原型切面
		if (aspectInstanceFactory.getAspectMetadata().isAnnotatedForClass(Lazy.class)) {
			// 原型切面的切点创建逻辑（待定）
			throw new FluoriteRuntimeException("原型切面的切点创建功能尚未完......");
//			this.pointcut = null;
//			this.lazy = true;
		} else {
			// 单例切面
			this.pointcut = this.declaredPointcut;
			this.lazy = false;
			this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
		}
	}

	private Advice instantiateAdvice(AspectJPluralisticPointcut pointcut) {
		Advice advice = this.aspectJAdvisorFactory.getAdvice(this.aspectJAdviceMethod, pointcut,
				this.aspectInstanceFactory, this.declarationOrder, this.aspectName);
		return (advice != null ? advice : EMPTY_ADVICE);
	}

	// --------------------------------------
	@Override
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	@Override
	public String getMethodName() {
		return methodName;
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public Method getAspectJAdviceMethod() {
		return aspectJAdviceMethod;
	}

	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

	@Override
	public Advice getAdvice() {
		if (this.instantiatedAdvice == null) {
			this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
		}
		return this.instantiatedAdvice;
	}

	@Override
	public int getOrder() {
		return this.aspectInstanceFactory.getOrder();
	}

	@Override
	public String getAspectName() {
		return this.aspectName;
	}

	@Override
	public int getDeclarationOrder() {
		return this.declarationOrder;
	}

	@Override
	public boolean isBeforeAdvice() {
		if (this.isBeforeAdvice == null) {
			determineAdviceType();
		}
		return this.isBeforeAdvice;
	}

	@Override
	public boolean isAfterAdvice() {
		if (this.isAfterAdvice == null) {
			determineAdviceType();
		}
		return this.isAfterAdvice;
	}

	@Override
	public boolean isLazy() {
		return this.lazy;
	}

	@Override
	public boolean isAdviceInstantiated() {
		return (this.instantiatedAdvice != null);
	}

	/** 确定当前Advice的类型 */
	private void determineAdviceType() {
		AspectJAnnotation<?> aspectJAnnotation = AbstractAspectJAdvisorFactory
				.findAspectJAnnotationOnMethod(this.aspectJAdviceMethod);
		if (aspectJAnnotation == null) {
			this.isBeforeAdvice = false;
			this.isAfterAdvice = false;
		} else {
			switch (aspectJAnnotation.getAnnotationType()) {
			case Pointcut:
			case Around:
				this.isBeforeAdvice = false;
				this.isAfterAdvice = false;
				break;
			case Before:
				this.isBeforeAdvice = true;
				this.isAfterAdvice = false;
				break;
			case After:
			case AfterReturning:
			case AfterThrowing:
				this.isBeforeAdvice = false;
				this.isAfterAdvice = true;
				break;
			}
		}
	}

	@Override
	public String toString() {
		return "InstantiationModelAwarePointcutAdvisorImpl [aspectJAdviceMethod=" + methodName
				+ ", declarationOrder=" + declarationOrder + ", aspectName=" + aspectName + ", pointcut=" + pointcut
				+ ", lazy=" + lazy + "]";
	}
	
	
}

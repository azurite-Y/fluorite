package org.zy.fluorite.aop.aspectj.support;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.zy.fluorite.aop.aspectj.advice.AbstractAspectJAdvice;
import org.zy.fluorite.aop.aspectj.advice.AspectJAfterAdvice;
import org.zy.fluorite.aop.aspectj.advice.AspectJAfterReturningAdvice;
import org.zy.fluorite.aop.aspectj.advice.AspectJAfterThrowingAdvice;
import org.zy.fluorite.aop.aspectj.advice.AspectJAroundAdvice;
import org.zy.fluorite.aop.aspectj.advice.AspectJMethodBeforeAdvice;
import org.zy.fluorite.aop.aspectj.advice.DeclareParentsAdvisor;
import org.zy.fluorite.aop.aspectj.annotation.Aspect;
import org.zy.fluorite.aop.aspectj.annotation.DeclareParents;
import org.zy.fluorite.aop.aspectj.annotation.Pointcut;
import org.zy.fluorite.aop.aspectj.interfaces.MetadataAwareAspectInstanceFactory;
import org.zy.fluorite.aop.exception.AopConfigException;
import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.function.ActiveFunction;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ConvertingComparator;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.InstanceComparator;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年7月6日 下午12:55:18;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
public class ReflectiveAspectJAdvisorFactory extends AbstractAspectJAdvisorFactory implements Serializable {
	private final BeanFactory beanFactory;

	private static final Comparator<Method> METHOD_COMPARATOR;
	
	static {
		METHOD_COMPARATOR = new ConvertingComparator(
				new InstanceComparator<>(AbstractAspectJAdvisorFactory.ASPECTJ_ANNOTATION_CLASSES),
				(ActiveFunction<Annotation , Method>)method -> {
					AspectJAnnotation<?> aspectJAnnotation = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(method);
					return (aspectJAnnotation == null ? null : aspectJAnnotation.getAnnotation());
		}) ;
	}
	public ReflectiveAspectJAdvisorFactory(BeanFactory beanFactory) {
		super();
		this.beanFactory = beanFactory;
	}

	@Override
	public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
		Class<?> beanType = aspectInstanceFactory.getType();
		String beanName = aspectInstanceFactory.getName();
		AnnotationMetadata aspectMetadata = aspectInstanceFactory.getAspectMetadata();
		super.validate(beanType);
		
		MetadataAwareAspectInstanceFactory lazySingletonAspectInstanceFactory =
				new LazySingletonAspectInstanceFactoryDecorator(aspectInstanceFactory);

		List<Advisor> advisors = new ArrayList<>();
		for (Method method : getAdvisorMethods(beanType,aspectMetadata)) {
			Advisor advisor = getAdvisor(method, lazySingletonAspectInstanceFactory, advisors.size(), beanName);
			if (advisor != null) {
				advisors.add(advisor);
			}
		}
		
		// 处理引入的方法
		ReflectionUtils.doWithLocalFields(beanType, field -> {
			Advisor advisor = getDeclareParentsAdvisor(field , aspectMetadata.getAnnotationAttributesForField(field));
			if (advisor != null) {
				advisors.add(advisor);
			}
			
		});
		return advisors;
	}

	@Override
	public Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory,
			int declarationOrder, String aspectName) {
		AspectJPluralisticPointcut expressionPointcut = getPointcut(candidateAdviceMethod, aspectInstanceFactory.getType());
		if (expressionPointcut == null) {
			return null;
		}

		return new InstantiationModelAwarePointcutAdvisorImpl(expressionPointcut, candidateAdviceMethod,
				this, aspectInstanceFactory, declarationOrder, aspectName);
	}

	@Override
	public Advice getAdvice(Method method, AspectJPluralisticPointcut expressionPointcut,
			MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {
		AspectJAnnotation<?> aspectJAnnotation =	AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(method);
		if (aspectJAnnotation == null) {
			return null;
		}
		if (!method.getDeclaringClass().isAnnotationPresent(Aspect.class)) {
			throw new AopConfigException("Advice必须在切面中声明，by method" +method.getName());
		}
		
//		DebugUtils.logFromAop(logger, "找到的切面方法："+method);
		AbstractAspectJAdvice advice;

		switch (aspectJAnnotation.getAnnotationType()) {
			case Pointcut:
				DebugUtils.logFromAop(logger, "找到的连接点方法："+method.getName()+"，class："+method.getDeclaringClass().getName());
				// 若连接点需在切面织入中进行任何逻辑则在此创建 AspectJPointcutAdvisor
				return null;
			case Around:
				DebugUtils.logFromAop(logger, "找到的@Around方法："+method.getName()+"，class："+method.getDeclaringClass().getName());
				advice = new AspectJAroundAdvice(method, expressionPointcut, aspectInstanceFactory);
				break;
			case Before:
				DebugUtils.logFromAop(logger, "找到的@Before方法："+method.getName()+"，class："+method.getDeclaringClass().getName());
				advice = new AspectJMethodBeforeAdvice(method, expressionPointcut, aspectInstanceFactory);
				break;
			case After:
				DebugUtils.logFromAop(logger, "找到的@After方法："+method.getName()+"，class："+method.getDeclaringClass().getName());
				advice = new AspectJAfterAdvice(method, expressionPointcut, aspectInstanceFactory);
				break;
			case AfterReturning:
				DebugUtils.logFromAop(logger, "找到的@AfterReturning方法："+method.getName()+"，class："+method.getDeclaringClass().getName());
				advice = new AspectJAfterReturningAdvice(method, expressionPointcut, aspectInstanceFactory);
				break;
			case AfterThrowing:
				DebugUtils.logFromAop(logger, "找到的@AfterThrowing方法："+method.getName()+"，class："+method.getDeclaringClass().getName());
				advice = new AspectJAfterThrowingAdvice(method, expressionPointcut, aspectInstanceFactory);
				for (Class<?> clz : method.getParameterTypes()) {
					if (Exception.class.isAssignableFrom(clz)) {
						advice.setThrowingType(clz);
					}
				}
				break;
			default:
				throw new UnsupportedOperationException("未知的advice类型，by method：" + method.getName() +"，class："+method.getDeclaringClass().getName());
		}	
		advice.setAspectName(aspectName);
		advice.setDeclarationOrder(declarationOrder);
//		String[] argNames = this.parameterNameDiscoverer.getParameterNames(method);
//		if (argNames != null) {
//			advice.setArgumentNamesFromStringArray(argNames);
//		}
		advice.calculateArgumentBindings();
		return advice;
	}

	/**
	 * 检查给定 bean 类型获取其中定义的 {@linkplain Pointcut 连接点 } 方法级
	 * @param beanType 检查的 bena 类型
	 * @param aspectMetadata - 给定类的注解信息
	 * @return
	 */
	private List<Method> getAdvisorMethods(Class<?> beanType, AnnotationMetadata aspectMetadata) {
		List<Method> methods = new ArrayList<>();
		ReflectionUtils.doWithLocalMethods(beanType, method -> {
			if (method.getAnnotations().length == 0) {return ;}
			
			// 方法上标注了注解则此对象一定不为null
			AnnotationAttributes attributesForMethod = aspectMetadata.getAnnotationAttributesForMethod(method);
			if (attributesForMethod.getAnnotation(Pointcut.class) == null) {
				methods.add(method);
			}
		});
		if (methods.size()>1) {
			methods.sort(METHOD_COMPARATOR);
		}
		return methods;
	}

	/**
	 * 查找属性上的 {@linkplain DeclareParents } 注解，并根据其属性创建 {@linkplain DeclareParentsAdvisor }
	 * @param field 检查的数下
	 * @param attributes 当前属性的注解信息
	 * @return
	 */
	private Advisor getDeclareParentsAdvisor(Field field , AnnotationAttributes attributes) {
		if (attributes == null) { return null; }
		
		DeclareParents declareParents = attributes.getAnnotation(DeclareParents.class);
		if (declareParents == null) {
			return null;
		}
		DebugUtils.logFromAop(logger, "找到的标注@DeclareParents注解的属性："+field.getName()+"，class："+field.getDeclaringClass().getName());
		Assert.isTrue(DeclareParents.class != declareParents.defaultImpl(), "'defaultImpl' 属性不能是DeclareParents类");
		Assert.isAssignable(field.getType(), declareParents.defaultImpl());
		return new DeclareParentsAdvisor(field.getType(), declareParents.value(), declareParents.defaultImpl());
	}

	/**
	 * 获得此切面方法的切点信息
	 * @param candidateAdviceMethod
	 * @param type
	 * @return
	 */
	private AspectJPluralisticPointcut getPointcut(Method candidateAdviceMethod , Class<?> type) {
		AspectJAnnotation<?> annotation = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
		AspectJPluralisticPointcut aspectJExpressionPointcut =	new AspectJPluralisticPointcut(type, new String[0], new Class<?>[0]);
		aspectJExpressionPointcut.setAspectJAnnotation(annotation);
		aspectJExpressionPointcut.setMethod(candidateAdviceMethod);
		if (this.beanFactory != null) {
			aspectJExpressionPointcut.setBeanFactory(this.beanFactory);
		}
		return aspectJExpressionPointcut;
	}
	
	class AspectJAdvisorMethod {
		private AnnotationAttributes attributes;
		private Method method;
		private AspectJAnnotation<?> aspectJAnnotation;
		public AspectJAdvisorMethod(AnnotationAttributes attributes, Method method,
				AspectJAnnotation<?> aspectJAnnotation) {
			super();
			this.attributes = attributes;
			this.method = method;
			this.aspectJAnnotation = aspectJAnnotation;
		}
		public AnnotationAttributes getAttributes() {
			return attributes;
		}
		public void setAttributes(AnnotationAttributes attributes) {
			this.attributes = attributes;
		}
		public Method getMethod() {
			return method;
		}
		public void setMethod(Method method) {
			this.method = method;
		}
		public AspectJAnnotation<?> getAspectJAnnotation() {
			return aspectJAnnotation;
		}
		public void setAspectJAnnotation(AspectJAnnotation<?> aspectJAnnotation) {
			this.aspectJAnnotation = aspectJAnnotation;
		}
	}
}

package org.zy.fluorite.aop.aspectj.support;

import java.lang.reflect.Method;

import org.zy.fluorite.aop.aspectj.support.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.zy.fluorite.beans.factory.aware.BeanFactoryAware;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月5日 下午3:58:42;
 * @author zy(azurite-Y);
 * @Description
 */
public class AspectJPluralisticPointcut  extends AbstractPluralisticPointcut implements BeanFactoryAware {
	
	private Class<?> pointcutDeclarationClass;

	private String[] pointcutParameterNames = new String[0];

	private Class<?>[] pointcutParameterTypes = new Class<?>[0];
	
	private Method aspectMethod;
	
	private BeanFactory beanFactory;

	private transient ClassLoader pointcutClassLoader;
	
	// 连接点匹配器
	private transient PointcutExpression pointcutExpression;
	
	public AspectJPluralisticPointcut() {}
	public AspectJPluralisticPointcut(Class<?> pointcutDeclarationClass, String[] pointcutParameterNames,
			Class<?>[] pointcutParameterTypes, AspectJAnnotation<?> aspectJAnnotation) {
		this(pointcutDeclarationClass, pointcutParameterNames, pointcutParameterTypes);
		setAspectJAnnotation(aspectJAnnotation);
	}

	public AspectJPluralisticPointcut(Class<?> declarationScope, String[] paramNames, Class<?>[] paramTypes) {
		this.pointcutDeclarationClass = declarationScope;
		Assert.isTrue(paramNames.length == paramTypes.length, "切入点参数名称的数目必须与切入点参数类型的数目匹配");
		this.pointcutParameterNames = paramNames;
		this.pointcutParameterTypes = paramTypes;
	}

	public Class<?> getPointcutDeclarationClass() {
		return pointcutDeclarationClass;
	}
	public void setPointcutDeclarationClass(Class<?> pointcutDeclarationClass) {
		this.pointcutDeclarationClass = pointcutDeclarationClass;
	}
	public String[] getPointcutParameterNames() {
		return pointcutParameterNames;
	}
	public void setPointcutParameterNames(String[] pointcutParameterNames) {
		this.pointcutParameterNames = pointcutParameterNames;
	}
	public Class<?>[] getPointcutParameterTypes() {
		return pointcutParameterTypes;
	}
	public void setPointcutParameterTypes(Class<?>[] pointcutParameterTypes) {
		this.pointcutParameterTypes = pointcutParameterTypes;
	}
	public ClassLoader getPointcutClassLoader() {
		return pointcutClassLoader;
	}
	public void setPointcutClassLoader(ClassLoader pointcutClassLoader) {
		this.pointcutClassLoader = pointcutClassLoader;
	}
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}
	public AspectJAnnotation<?> getAspectJAnnotation() {
		return aspectJAnnotation;
	}
	public void setAspectJAnnotation(AspectJAnnotation<?> aspectJAnnotation) {
		this.aspectJAnnotation = aspectJAnnotation;
	}
	public Method getMethod() {
		return aspectMethod;
	}
	public void setMethod(Method method) {
		this.aspectMethod = method;
	}
	
	@Override
	public String getExpression() {
		return (String) super.aspectJAnnotation.getAnnotationValue("value");
	}

	@Override
	public String getPrefix() {
		return (String) super.aspectJAnnotation.getAnnotationValue("prefix");
	}

	@Override
	public int getAccessModifier() {
		return (int) super.aspectJAnnotation.getAnnotationValue("accessModifier");
	}

	@Override
	public String getReturnType() {
		return (String) super.aspectJAnnotation.getAnnotationValue("returnType");
	}

	@Override
	public String getArgNames() {
		return (String) super.aspectJAnnotation.getAnnotationValue("argNames");
	}
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
 
	@Override
	public boolean matcher(Class<?> targetClass) {
		if (!Assert.hasText(getExpression())) {
			throw new IllegalStateException("在尝试匹配之前必须设置切点表达式，by annotation："+aspectJAnnotation.getAnnotationName()
			+"class ：" + this.pointcutDeclarationClass);
		}
		if (this.pointcutExpression == null) {
			this.pointcutExpression = PointcutExpression.buildPointcutExpression(this.pointcutClassLoader);
		}
		return this.pointcutExpression.parse(targetClass, aspectJAnnotation, this.pointcutDeclarationClass, aspectMethod);
	}
	
	@Override
	public boolean matcher(Class<?> targetClass, Method method) {
		return this.pointcutExpression.parse(targetClass, method , aspectJAnnotation, this.pointcutDeclarationClass, this.aspectMethod);
	}
}

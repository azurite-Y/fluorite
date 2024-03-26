package org.zy.fluorite.aop.aspectj.expression;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.aop.aspectj.interfaces.PointcutExpressionParse;
import org.zy.fluorite.aop.aspectj.support.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.zy.fluorite.aop.aspectj.support.PointcutMatcher;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年11月27日 下午2:01:40;
 * @author zy(azurite-Y);
 * @Description Aspectj-@within的切点匹配器实现，匹配指定注解标注类中的所有方法
 */
public class ClassAnnotationPointcutExpressionParse implements PointcutExpressionParse{
	public static final String CLASS_ANNOTATION = "@within";
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	@SuppressWarnings("unchecked")
	@Override
	public PointcutMatcher parse(Class<?> targetClass, AspectJAnnotation<?> aspectJAnnotation, Class<?> aspectClass,
			Method joinPointcutMethod) {
		String annotationValue = aspectJAnnotation.getAnnotationValue("value", String.class);
		Assert.hasText(annotationValue, "目标切点标注注解全限定类名无效，by："+targetClass);
		
		Class<? extends Annotation> anoClz = null;
		try {
			anoClz = (Class<? extends Annotation>) Class.forName(annotationValue);
		} catch (ClassNotFoundException e) {
			logger.error("实例化目标切点标注注解失败，by："+annotationValue);
			e.printStackTrace();
		}
		
		PointcutMatcher pointcutMatcher = new PointcutMatcher();
		
		// 判断目标类是否标注指定注解
		if (targetClass.isAnnotationPresent(anoClz)) {
			pointcutMatcher.setMark(true);
			pointcutMatcher.setMatcherMethods(true);
		}
		return pointcutMatcher;
	}
	
	@Override
	public boolean support(String prefix) {
		return CLASS_ANNOTATION.equalsIgnoreCase(prefix);
	}
}

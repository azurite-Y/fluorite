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
 * @DateTime 2020年11月27日 下午2:01:50;
 * @author zy(azurite-Y);
 * @Description Aspectj-@annotation的切点匹配器实现，匹配指定注解标注的方法
 */
public class MethodAnnotationPointcutExpressionParse implements PointcutExpressionParse{
	public static final String METHOD_ANNOTATION = "@annotation";
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
		
		Method[] methods = targetClass.getMethods();
		// 迭代Method数组获取其参数集，然后判断参数集是否标注指定注解
		for (Method method : methods) {
			/*
			 * 	若标注了则将当前Method对象保存到PointcutMatcher对象中并返回
			 * 	若未标注则返回exipre属性为false的PointcutMatcher对象
			 */
			if (method.isAnnotationPresent(anoClz)) {
				pointcutMatcher.setMark(true);
				pointcutMatcher.setAspectJJoinPointcutAnnotation(aspectJAnnotation);
				pointcutMatcher.addPointcutMethod(method);
			}
		}
		
		// 判断目标类方法是否标注指定注解
		
		return pointcutMatcher;
	}

	@Override
	public boolean support(String prefix) {
		return METHOD_ANNOTATION.equalsIgnoreCase(prefix);
	}
}

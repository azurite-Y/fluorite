package org.zy.fluorite.aop.aspectj.expression;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.aop.aspectj.interfaces.PointcutExpressionParse;
import org.zy.fluorite.aop.aspectj.support.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.zy.fluorite.aop.aspectj.support.PointcutMatcher;
import org.zy.fluorite.core.annotation.Value;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年11月27日 下午2:01:56;
 * @author zy(azurite-Y);
 * @Description Aspectj-@args的切点匹配器实现，匹配指定注解标注参数的目标方法
 */
public class ArgsAnnotationPointcutExpressionParse implements PointcutExpressionParse {
	public static final String ARGS_ANNOTATION = "@args";
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
			 *	 若未标注则返回exipre属性为false的PointcutMatcher对象
			 */
			if (isAnnotationPresentForParameters(anoClz , method.getParameters()) ) {
				pointcutMatcher.setMark(true);
				pointcutMatcher.setAspectJJoinPointcutAnnotation(aspectJAnnotation);
				pointcutMatcher.addPointcutMethod(method);
			}
		}
		
		return pointcutMatcher;
	}
	
	private boolean isAnnotationPresentForParameters(Class<? extends Annotation> anoClz , Parameter... parameters) {
		for (Parameter parameter : parameters) {
			if( parameter.isAnnotationPresent(anoClz) ) {
				return true;		
			}
		}
		return false;
		
	}

	@Override
	public boolean support(String prefix) {
		return ARGS_ANNOTATION.equalsIgnoreCase(prefix);
	}

	public static void main(@Value(value = "")  String[] args) throws NoSuchMethodException, SecurityException {
		String name = "ab,c";
		String[] split = name.split(",");		List<String> asList = Arrays.asList(split);
		System.out.println(asList);
		Method[] method = ArgsAnnotationPointcutExpressionParse.class.getMethods();

		System.out.println(method[1].getName());
//		Annotation[][] parameterAnnotations = method[0].getParameterAnnotations();
//		System.out.println(parameterAnnotations.length);
//		System.out.println(((Annotation) parameterAnnotations[0][0]).annotationType().getCanonicalName());
		
		Parameter[] parameters = method[1]. getParameters();
		for (Parameter parameter : parameters) {
			System.out.println(parameter.isAnnotationPresent(Value.class));
		}

	}
	public static void test() {}
}

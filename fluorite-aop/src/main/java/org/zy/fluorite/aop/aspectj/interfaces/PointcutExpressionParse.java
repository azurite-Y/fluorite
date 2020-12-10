package org.zy.fluorite.aop.aspectj.interfaces;

import java.lang.reflect.Method;

import org.zy.fluorite.aop.aspectj.support.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.zy.fluorite.aop.aspectj.support.PointcutMatcher;

/**
 * @DateTime 2020年7月9日 下午11:26:31;
 * @author zy(azurite-Y);
 * @Description 切点表达式解析器，负责切点关联和解析
 */
public interface PointcutExpressionParse {
	public static final String WILDCARD_CHARACTER = "*";
	public static final String DOUBLE_DELIMITRES = "..";
	public static final String DELIMITRES = ".";
	
	/** org.zy.fluorite.aop.aspectj.interfaces.PointcutExpressionParse */
	public static final String PROPERTY_KEY = PointcutExpressionParse.class.getName();
	
	/**
	 * 根据给定的切面信息判断此切面方法是否适配与当前Bean
	 * @param targetClass - 适配Bean的Class对象
	 * @param aspectJAnnotation - 切面连接点方法的注解包装对象
	 * @param aspectClass - 切面类
	 * @param method - 切面连接点方法
	 * @return 返回{@linkplain #PointcutMatcher } 对象表达匹配结果，若匹配成功则返回对象的expire属性为true，反之则为false
	 */
	PointcutMatcher parse(Class<?> targetClass , AspectJAnnotation<?>  aspectJAnnotation ,  Class<?> aspectClass , Method method);
	
	/**
	 * 根据切点表达式的语义补正确定要使用的切点匹配器
	 * @param prefix
	 * @return
	 */
	boolean support(String prefix);
}

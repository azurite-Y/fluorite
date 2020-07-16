package org.zy.fluorite.aop.interfaces;

/**
 * @DateTime 2020年7月9日 下午4:59:36;
 * @author zy(azurite-Y);
 * @Description 多元化的切点表达样式接口
 */
public interface PluralisticPointcut extends Pointcut {
	/** 注解的value属性 */
	String getExpression();
	
	/** 注解的prefix属性 */
	String getPrefix();
	
	/** 注解的accessModifier属性 */
	int getAccessModifier();
	
	/** 注解的returnType属性 */
	String getReturnType();
	
	/** 注解的argNames属性 */
	String getArgNames();
}

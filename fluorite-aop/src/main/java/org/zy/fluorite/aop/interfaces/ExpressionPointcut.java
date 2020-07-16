package org.zy.fluorite.aop.interfaces;

/**
 * @DateTime 2020年7月5日 下午3:49:24;
 * @author zy(azurite-Y);
 * @Description 由使用字符串表达式的切点实现的接口
 */
public interface ExpressionPointcut extends Pointcut {
	/**
	 * Return the String expression for this pointcut.
	 */
	String getExpression();
}

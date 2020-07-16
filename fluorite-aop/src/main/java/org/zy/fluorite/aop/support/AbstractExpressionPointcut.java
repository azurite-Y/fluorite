package org.zy.fluorite.aop.support;

import org.zy.fluorite.aop.interfaces.ExpressionPointcut;

/**
 * @DateTime 2020年7月5日 下午3:50:35;
 * @author zy(azurite-Y);
 * @Description 表达式切点的抽象超类，提供位置和表达式属性
 */
public abstract class AbstractExpressionPointcut implements ExpressionPointcut {
	/**  */
	private String location;

	private String expression;

	@Override
	public String getExpression() {
		return this.expression;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setExpression(String expression) {
		this.expression = expression;
		try {
			onSetExpression(expression);
		} catch (IllegalArgumentException ex) {
			if (this.location != null) {
				throw new IllegalArgumentException("无效的表达式位置：[" + this.location + "]: " + ex);
			} else {
				throw ex;
			}
		}
	}

	/** 当一个新的切入点表达式被设置时调用。如果可能的话，应该在此时解析表达式 */
	protected void onSetExpression(String expression) {}

}

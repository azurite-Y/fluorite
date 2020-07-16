package org.zy.fluorite.beans.support;

import java.lang.reflect.Method;

import org.zy.fluorite.core.interfaces.BeanMetadataElement;
import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午1:30:38;
 * @Description 重写方法对象
 */
public abstract class MethodOverride implements BeanMetadataElement {
	// 重写方法名
	private final String methodName;
	private boolean overloaded = true;
	// 定义重写方法的类
	private Object source;

	protected MethodOverride(String methodName) {
		Assert.notNull(methodName, "Method不能为null");
		this.methodName = methodName;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public void setOverloaded(boolean overloaded) {
		this.overloaded = overloaded;
	}

	public boolean isOverloaded() {
		return this.overloaded;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	@Override
	public Object getSource() {
		return this.source;
	}

	/**
	 * 子类必须重写此项以指示它们是否与给定方法匹配。这允许参数列表检查和方法名称检查
	 * 
	 * @param 需要检查的Method
	 * @return 此重写是否与给定方法匹配
	 */
	public abstract boolean matches(Method method);

}

package org.zy.fluorite.beans.support;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午8:06:31;
 * @Description MethodOverride的扩展，表示IoC容器对方法的任意重写。
 * 可以重写任何非final方法，而不管其参数和返回类型如何
 */
public class ReplaceOverride extends MethodOverride {

	private final String methodReplacerBeanName;

	private List<String> typeIdentifiers = new LinkedList<>();
	
	protected ReplaceOverride(String methodName, String methodReplacerBeanName) {
		super(methodName);
		Assert.notNull(methodName, "方法替换程序bean名称不能为空");
		this.methodReplacerBeanName = methodReplacerBeanName;
	}

	public String getMethodReplacerBeanName() {
		return this.methodReplacerBeanName;
	}

	/**
	 * 添加类型标识符，如“Exception”或java.lang.Exc，以标识参数类型
	 */
	public void addTypeIdentifier(String identifier) {
		this.typeIdentifiers.add(identifier);
	}

	@Override
	public boolean matches(Method method) {
		if (!method.getName().equals(getMethodName())) {
			return false;
		}
		if (!isOverloaded()) {
			// 不重载：不用担心arg类型匹配...
			return true;
		}
		// 判断参数个数是否相等
		if (this.typeIdentifiers.size() != method.getParameterCount()) {
			return false;
		}
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < this.typeIdentifiers.size(); i++) {
			String identifier = this.typeIdentifiers.get(i);
			if (!parameterTypes[i].getName().contains(identifier)) {
				return false;
			}
		}
		return true;
	}
}

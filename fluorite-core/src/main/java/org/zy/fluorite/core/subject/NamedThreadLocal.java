package org.zy.fluorite.core.subject;

import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月8日 下午5:07:18;
 * @param <T> 属性值类型
 * @Description
 */
public class NamedThreadLocal<T> extends ThreadLocal<T> {
	/** 指定此ThreadLocal实现的作用域 */
	private final String name;

	public NamedThreadLocal(String name) {
		Assert.hasText(name, "name属性不能为null或空串");
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}

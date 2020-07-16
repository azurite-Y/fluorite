package org.zy.fluorite.aop.support;

import java.io.Serializable;

import org.zy.fluorite.aop.interfaces.function.ClassFilter;

/**
 * @DateTime 2020年7月4日 下午3:42:22;
 * @author zy(azurite-Y);
 * @Description 匹配所有类的规范ClassFilter实例
 */
@SuppressWarnings("serial")
public final class TrueClassFilter implements ClassFilter, Serializable {
	public static final TrueClassFilter INSTANCE = new TrueClassFilter();
	
	private TrueClassFilter() {}

	@Override
	public boolean matches(Class<?> clazz) {
		return true;
	}

	/** 防止反序列化创建本类的实例 */
	private Object readResolve() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return "ClassFilter.TRUE";
	}
}
package org.zy.fluorite.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.zy.fluorite.aop.interfaces.MethodMatcher;

/**
 * @DateTime 2020年7月5日 下午3:03:11;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public final class TrueMethodMatcher implements MethodMatcher, Serializable {
	public static final TrueMethodMatcher INSTANCE = new TrueMethodMatcher();

	private TrueMethodMatcher() {}

	@Override
	public boolean isRuntime() {
		return false;
	}

	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		return true;
	}

	@Override
	public boolean matches(Method method, Class<?> targetClass, Object... args) {
		throw new UnsupportedOperationException();
	}


	@Override
	public String toString() {
		return "MethodMatcher.TRUE";
	}

	private Object readResolve() {
		return INSTANCE;
	}
}
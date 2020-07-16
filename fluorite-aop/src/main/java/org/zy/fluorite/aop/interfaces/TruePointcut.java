package org.zy.fluorite.aop.interfaces;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @DateTime 2020年7月5日 下午2:45:41;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
final class TruePointcut implements Pointcut, Serializable  {
	public static final TruePointcut INSTANCE = new TruePointcut();

	private TruePointcut() {}

	private Object readResolve() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return "Pointcut.TRUE";
	}

	@Override
	public boolean matcher(Class<?> clz) {
		return true;
	}

	@Override
	public boolean matcher(Class<?> targetClass, Method method) {
		return true;
	}
}

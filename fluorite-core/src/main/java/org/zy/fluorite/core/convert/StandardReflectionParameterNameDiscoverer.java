package org.zy.fluorite.core.convert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.zy.fluorite.core.interfaces.ParameterNameDiscoverer;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月12日 上午9:44:33;
 * @Description
 */
public class StandardReflectionParameterNameDiscoverer implements ParameterNameDiscoverer {

	@Override
	public String[] getParameterNames(Method method) {
		return getParameterNames(method.getParameters());
	}
	
	@Override
	public String[] getParameterNames(Constructor<?> ctor) {
		return getParameterNames(ctor.getParameters());
	}

	private String[] getParameterNames(Parameter[] parameters) {
		String[] parameterNames = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			Parameter param = parameters[i];
			if (!param.isNamePresent()) {
				return null;
			}
			parameterNames[i] = param.getName();
		}
		return parameterNames;
	}

}

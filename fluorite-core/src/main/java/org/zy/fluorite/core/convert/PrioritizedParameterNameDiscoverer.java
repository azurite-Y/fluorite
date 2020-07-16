package org.zy.fluorite.core.convert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.zy.fluorite.core.interfaces.ParameterNameDiscoverer;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月12日 上午9:35:39;
 * @Description ParameterNameDiscoverer实现，它连续尝试多个DiscoveredElegate。
 *              在addDiscoverer方法中最先添加的那些具有最高优先级。如果一个返回空值，将尝试下一个。
 *              默认行为是，如果没有匹配的发现者，则返回null。
 */
public class PrioritizedParameterNameDiscoverer implements ParameterNameDiscoverer {
	private final List<ParameterNameDiscoverer> parameterNameDiscoverers = new LinkedList<>();

	public PrioritizedParameterNameDiscoverer() {
		this.parameterNameDiscoverers.add(new StandardReflectionParameterNameDiscoverer());
	}

	@Override
	public String[] getParameterNames(Constructor<?> ctor) {
		for (ParameterNameDiscoverer pnd : this.parameterNameDiscoverers) {
			String[] result = pnd.getParameterNames(ctor);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public String[] getParameterNames(Method executable) {
		for (ParameterNameDiscoverer pnd : this.parameterNameDiscoverers) {
			String[] result = pnd.getParameterNames(executable);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

}

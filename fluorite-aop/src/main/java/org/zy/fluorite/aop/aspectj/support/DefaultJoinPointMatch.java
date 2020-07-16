package org.zy.fluorite.aop.aspectj.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zy.fluorite.aop.aspectj.interfaces.JoinPointMatch;
import org.zy.fluorite.core.utils.TypeConvertUtils;

/**
 * @DateTime 2020年7月12日 下午5:26:49;
 * @author zy(azurite-Y);
 * @Description 默认的参数绑定器实现，但只绑定目标方法的参数。
 */
public class DefaultJoinPointMatch implements JoinPointMatch{
	private List<Class<?>> types = new ArrayList<>();
	
	private Object[] arguments;
	
	public DefaultJoinPointMatch(Object[] arguments) {
		super();
		this.arguments = arguments;
		for (Object object : arguments) {
			types.add(object.getClass());
		}
	}

	@Override
	public Object parameterBinding(Class<?> targetClass, String parameterName) {
		// 后续改进：考虑参数的泛型是否匹配，不匹配则TypeConvertUtils继续类型转换，不能转换则抛出异常
		Class<?> clz = null;
		Object convertResult = null;
		for (int i = 0; i < types.size(); i++) {
			clz = types.get(i);
			if (targetClass.isAssignableFrom(clz)) {
				return arguments[i];
			} else {
				 convertResult = TypeConvertUtils.convertRestrainException(arguments[i], targetClass);
				 if (convertResult != null) {
					 return convertResult;
				 }
			}
		}
		return null;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(arguments);
		result = prime * result + ((types == null) ? 0 : types.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultJoinPointMatch other = (DefaultJoinPointMatch) obj;
		if (!Arrays.deepEquals(arguments, other.arguments))
			return false;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		return true;
	}

	
}

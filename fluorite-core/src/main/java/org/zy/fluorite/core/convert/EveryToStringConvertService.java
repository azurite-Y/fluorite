package org.zy.fluorite.core.convert;

import org.zy.fluorite.core.exception.TypeMismatchException;
import org.zy.fluorite.core.interfaces.ConversionService;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月2日 上午9:45:59;
 * @author zy(azurite-Y);
 * @Description 将非String类型为String类型
 */
public class EveryToStringConvertService implements ConversionService<Object, String> {

	@Override
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		return !(String.class.isAssignableFrom(sourceType)) && String.class.isAssignableFrom(targetType);
	}

	@Override
	public String convert(Object source, Class<?> targetType)  throws TypeMismatchException {
		Assert.isAssignable(String.class, targetType);
		if (String.class.isAssignableFrom(targetType)) {
			return String.valueOf(source);
		}
		throw new TypeMismatchException("类型不匹配，by targetType："+targetType+"，此转换器只支持转换为String类型");
	}

}

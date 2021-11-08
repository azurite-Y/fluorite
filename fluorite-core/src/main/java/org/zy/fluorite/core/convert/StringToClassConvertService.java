package org.zy.fluorite.core.convert;

import org.zy.fluorite.core.exception.TypeMismatchException;
import org.zy.fluorite.core.interfaces.ConversionService;

/**
 * @DateTime 2020年7月2日 上午12:53:14;
 * @author zy(azurite-Y);
 * @Description
 */
public class StringToClassConvertService implements ConversionService<String,Class<?>> {

	@Override
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		return String.class.isAssignableFrom(sourceType) && Class.class.isAssignableFrom(targetType);
	}

	@Override
	public Class<?> convert(Object source, Class<?> targetType)  throws TypeMismatchException {
		String sourceStr = String.valueOf(source);
		try {
			return Class.forName(sourceStr);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		};
		throw new TypeMismatchException("类型不匹配，by targetType："+targetType+"，此转换器只支持转换为Class类型");
	}

}

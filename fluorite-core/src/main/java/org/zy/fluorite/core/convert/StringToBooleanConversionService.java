package org.zy.fluorite.core.convert;

import org.zy.fluorite.core.exception.TypeMismatchException;
import org.zy.fluorite.core.interfaces.ConversionService;

/**
 * @dateTime 2022年12月10日;
 * @author zy(azurite-Y);
 * @description {@link String} 类型转换为 {@link Boolean } 类型
 */
public class StringToBooleanConversionService implements ConversionService<String,Boolean> {
	
	@Override
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		return String.class.isAssignableFrom(sourceType) && ( boolean.class.isAssignableFrom(targetType) || Boolean.class.isAssignableFrom(targetType));
	}
	
	@Override
	public Boolean convert(Object source, Class<?> clz) throws TypeMismatchException {
		String sourceStr = String.valueOf(source);
		return Boolean.valueOf(sourceStr);
	}
	
}

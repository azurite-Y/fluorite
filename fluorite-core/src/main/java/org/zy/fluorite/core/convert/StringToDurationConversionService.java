package org.zy.fluorite.core.convert;

import java.time.Duration;

import org.zy.fluorite.core.exception.TypeMismatchException;
import org.zy.fluorite.core.interfaces.ConversionService;

/**
 * @dateTime 2022年12月10日;
 * @author zy(azurite-Y);
 * @description {@link String} 类型转换为 {@link Boolean } 类型
 */
public class StringToDurationConversionService implements ConversionService<String,Duration> {
	
	@Override
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		return String.class.isAssignableFrom(sourceType) && Duration.class.isAssignableFrom(targetType);
	}
	
	@Override
	public Duration convert(Object source, Class<?> clz) throws TypeMismatchException {
		String sourceStr = String.valueOf(source);
		return Duration.ofMillis(Long.valueOf(sourceStr));
	}
}

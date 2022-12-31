package org.zy.fluorite.core.convert;

import java.nio.charset.Charset;

import org.zy.fluorite.core.exception.TypeMismatchException;
import org.zy.fluorite.core.interfaces.ConversionService;

/**
 * @DateTime 2020年7月2日 上午12:53:14;
 * @author zy(azurite-Y);
 * @Description
 */
public class StringToCharsetConvertService implements ConversionService<String,Charset> {

	@Override
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		return String.class.isAssignableFrom(sourceType) && Charset.class.isAssignableFrom(targetType);
	}

	@Override
	public Charset convert(Object source, Class<?> targetType)  throws TypeMismatchException {
		String sourceStr = String.valueOf(source);
		return Charset.forName(sourceStr);
	}

}

package org.zy.fluorite.core.convert;

import java.io.File;

import org.zy.fluorite.core.exception.TypeMismatchException;
import org.zy.fluorite.core.interfaces.ConversionService;

/**
 * @DateTime 2020年7月2日 上午12:53:14;
 * @author zy(azurite-Y);
 * @Description
 */
public class StringToFileConvertService implements ConversionService<String,File> {

	@Override
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		return String.class.isAssignableFrom(sourceType) && File.class.isAssignableFrom(targetType);
	}

	@Override
	public File convert(Object source, Class<?> targetType)  throws TypeMismatchException {
		String sourceStr = String.valueOf(source);
		return new File(sourceStr);
	}

}

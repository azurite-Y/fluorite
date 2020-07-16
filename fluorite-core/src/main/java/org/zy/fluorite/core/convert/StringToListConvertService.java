package org.zy.fluorite.core.convert;

import java.util.List;

import org.zy.fluorite.core.exception.TypeMismatchException;
import org.zy.fluorite.core.interfaces.ConversionService;
import org.zy.fluorite.core.utils.CollectionUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年7月2日 上午12:53:14;
 * @author zy(azurite-Y);
 * @Description
 */
public class StringToListConvertService implements ConversionService<String,List<?>> {

	@Override
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		return String.class.isAssignableFrom(sourceType) && List.class.isAssignableFrom(targetType);
	}

	@Override
	public List<?> convert(Object source, Class<?> targetType)  throws TypeMismatchException {
		String sourceStr = String.valueOf(source);
		String[] tokenizeToStringArray = StringUtils.tokenizeToStringArray(sourceStr, ",.\t\n", null);
		return CollectionUtils.asList(tokenizeToStringArray);
	}

}

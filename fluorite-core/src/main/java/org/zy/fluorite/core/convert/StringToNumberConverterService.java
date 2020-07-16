package org.zy.fluorite.core.convert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zy.fluorite.core.exception.TypeMismatchException;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月1日 下午11:24:19;
 * @author zy(azurite-Y);
 * @Description 纯数字组成的String类型转换为基本数据类型
 */
public class StringToNumberConverterService extends AbstractNumberConvertService<String> {
	public StringToNumberConverterService() {}
	
	@Override
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		return String.class.isAssignableFrom(sourceType) && list.contains(targetType);
	}

	@Override
	public Number convert(Object source, Class<?> targetType)  throws TypeMismatchException {
		Assert.isAssignable(String.class, source.getClass());
		String sourceStr = String.valueOf(source);
		Pattern pattern = Pattern.compile("^[0-9]*$");
		Matcher matcher = pattern.matcher(sourceStr);
		if (!matcher.matches()) {
			throw new TypeMismatchException("无法转换的字符串，by："+sourceStr);
		}
		
		if (targetType.isAssignableFrom(byte.class)) {
			return Byte.parseByte(sourceStr);
		}else if (targetType.isAssignableFrom(short.class)) {
			return Short.parseShort(sourceStr);
		}else if (targetType.isAssignableFrom(int.class)) {
			return Integer.parseInt(sourceStr);
		}else if (targetType.isAssignableFrom(long.class)) {
			return Long.parseLong(sourceStr);
		}else if (targetType.isAssignableFrom(float.class)) {
			return Float.parseFloat(sourceStr);
		}else if (targetType.isAssignableFrom(double.class)) {
			return Double.parseDouble(sourceStr);
		}else if (targetType.isAssignableFrom(Byte.class)) {
			return Byte.valueOf(sourceStr);
		}else if (targetType.isAssignableFrom(Short.class)) {
			return Short.valueOf(sourceStr);
		}else if (targetType.isAssignableFrom(Integer.class)) {
			return Integer.valueOf(sourceStr);
		}else if (targetType.isAssignableFrom(Long.class)) {
			return Long.valueOf(sourceStr);
		}else if (targetType.isAssignableFrom(Float.class)) {
			return Float.valueOf(sourceStr);
		}else if (targetType.isAssignableFrom(Double.class)) {
			return Double.valueOf(sourceStr);
		}else {
			throw new TypeMismatchException("类型不匹配，by targetType："+targetType+"，需为"+list+"其中之一");
		}
	}
}

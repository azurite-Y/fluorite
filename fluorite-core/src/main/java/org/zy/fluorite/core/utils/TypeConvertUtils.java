package org.zy.fluorite.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.core.convert.EveryToStringConvertService;
import org.zy.fluorite.core.convert.NumberToNumberConvertService;
import org.zy.fluorite.core.convert.StringToListConvertService;
import org.zy.fluorite.core.convert.StringToNumberConverterService;
import org.zy.fluorite.core.exception.TypeMismatchException;
import org.zy.fluorite.core.interfaces.ConversionService;

/**
 * @DateTime 2020年7月1日 下午11:16:25;
 * @author zy(azurite-Y);
 * @Description {@link ConversionService}
 */
public class TypeConvertUtils {

	public static final List<ConversionService<?,?>> converts = new ArrayList<>();

	static {
		converts.add(new NumberToNumberConvertService());
		converts.add(new StringToNumberConverterService());
		converts.add(new StringToListConvertService());
		converts.add(new EveryToStringConvertService());
	}
	
	/**
	 * 使用所有的类型转换器，将给定对象转换为期望的类型，若无法转换则抛出异常
	 * @param obj - 需要类型转换的对象
	 * @param clz - 目标类型
	 * @return 
	 * @see NumberToNumberConvertService
	 * @see StringToNumberConverterService
	 * @see StringToListConvertService
	 * @see EveryToStringConvertService
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convert(Object obj, Class<T> clz) throws Exception {
		Class<?> objClz = obj.getClass();
		for (ConversionService<?,?> conversionService : converts) {
			if (conversionService.canConvert(objClz, clz)) {
				return (T) conversionService.convert(obj, clz);
			}
		}
		throw new TypeMismatchException("不支持的类型转换，by targetType：" + clz + "，另请参阅ConversionService接口实现");
	}

	/**
	 * 使用所有的类型转换器，将给定对象转换为期望的类型，若无法转换则返回null
	 * @param <T>
	 * @param obj - 需要转换的对象
	 * @param sourceType - 源类型
	 * @param targetType - 期望类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convertRestrainException(Object obj, Class<T> targetType) {
		Class<?> objClz = obj.getClass();
		
		try {
			for (ConversionService<?,?> conversionService : converts) {
				if (conversionService.canConvert(objClz, targetType)) {
					return (T) conversionService.convert(obj, targetType);
				}
			}
		} catch (TypeMismatchException e) {
			return null;
		}
		return null;
	}
}

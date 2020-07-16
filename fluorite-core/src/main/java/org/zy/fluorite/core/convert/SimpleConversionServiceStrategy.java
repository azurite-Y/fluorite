package org.zy.fluorite.core.convert;

import java.util.List;

import org.zy.fluorite.core.exception.TypeMismatchException;
import org.zy.fluorite.core.interfaces.ConversionService;
import org.zy.fluorite.core.interfaces.ConversionServiceStrategy;
import org.zy.fluorite.core.utils.TypeConvertUtils;

/**
 * @DateTime 2020年7月2日 上午10:38:07;
 * @author zy(azurite-Y);
 * @Description 默认的类型转换服务调用策略
 */
public class SimpleConversionServiceStrategy implements ConversionServiceStrategy {
	private final List<ConversionService<?,?>> converts;

	public SimpleConversionServiceStrategy() {
		converts = TypeConvertUtils.converts;
	}
	
	/**
	 * @param <T>
	 * @param obj - 需要类型转换的对象
	 * @param clz - 目标类型
	 * @return
	 * @throws Exception 
	 * @see NumberToNumberConvertService
	 * @see StringToNumberConverterService
	 * @see StringToListConvertService
	 * @see EveryToStringConvertService
	 */
	@SuppressWarnings("unchecked")
	public <T> T convert(Object obj, Class<T> clz) throws TypeMismatchException {
		if (clz.isInstance(obj)) {
			return (T)obj;
		}
		Class<?> objClz = obj.getClass();
		for (ConversionService<?,?> conversionService : converts) {
			if (conversionService.canConvert(objClz, clz)) {
				return (T) conversionService.convert(obj, clz);
			}
		}
		throw new TypeMismatchException("不支持的类型转换，by sourceType："+objClz+"，targetType：" + clz + "，另请参阅ConversionService接口实现");
	}
}

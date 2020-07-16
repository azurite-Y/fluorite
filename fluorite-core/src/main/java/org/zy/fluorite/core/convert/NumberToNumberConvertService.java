package org.zy.fluorite.core.convert;

import org.zy.fluorite.core.exception.TypeMismatchException;

/**
 * @DateTime 2020年7月2日 上午12:16:11;
 * @author zy(azurite-Y);
 * @Description 基本数据类型之间的互转
 */
public class NumberToNumberConvertService extends  AbstractNumberConvertService<Number> {
	public NumberToNumberConvertService() {super();}

	@Override
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		return list.contains(sourceType) && list.contains(targetType);
	}

	@Override
	public Number convert(Object source, Class<?> targetType)  throws TypeMismatchException  {
//		Assert.isAssignable(Number.class, targetType);
		Number sourceNum = (Number)source;
		if (targetType.isAssignableFrom(byte.class)) {
			return sourceNum.byteValue();
		}else if (targetType.isAssignableFrom(short.class)) {
			return sourceNum.shortValue();
		}else if (targetType.isAssignableFrom(int.class)) {
			return sourceNum.intValue();
		}else if (targetType.isAssignableFrom(long.class)) {
			return sourceNum.longValue();
		}else if (targetType.isAssignableFrom(float.class)) {
			return sourceNum.floatValue();
		}else if (targetType.isAssignableFrom(double.class)) {
			return sourceNum.doubleValue();
		}else if (targetType.isAssignableFrom(Byte.class)) {
			return Byte.valueOf(sourceNum.byteValue());
		}else if (targetType.isAssignableFrom(Short.class)) {
			return Short.valueOf(sourceNum.shortValue());
		}else if (targetType.isAssignableFrom(Integer.class)) {
			return Integer.valueOf(sourceNum.intValue());
		}else if (targetType.isAssignableFrom(Long.class)) {
			return Long.valueOf(sourceNum.longValue());
		}else if (targetType.isAssignableFrom(Float.class)) {
			return Float.valueOf(sourceNum.floatValue());
		}else if (targetType.isAssignableFrom(Double.class)) {
			return Double.valueOf(sourceNum.doubleValue());
		}else {
			throw new TypeMismatchException("类型不匹配，by targetType："+targetType+"，需为"+super.list+"其中之一");
		}
	}

}

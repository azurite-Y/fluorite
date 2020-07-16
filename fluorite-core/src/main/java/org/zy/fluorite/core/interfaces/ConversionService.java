package org.zy.fluorite.core.interfaces;

import org.zy.fluorite.core.exception.TypeMismatchException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午2:17:32;
 * @Description 转换服务接口
 * @param <S> - 转换的源类型
 * @param <S> - 转换的目标类型
 */
public interface ConversionService<S,R> {
	/**
	 * 如果sourceType的对象可以转换为targetType，则返回true。
	 * 如果此方法返回true，则表示convert（Object，Class）能够将sourceType的实例转换为targetType。
	 * <p>关于集合、数组和映射的特别说明类型：用于转换在集合、数组和映射类型之间，
	 * 即使在底层元素不可转换时转换调用仍可能生成ConversionException，此方法仍将返回True。
	 * 在处理集合和映射时，调用方需要处理这种异常情况。</p>
	 */
	default boolean canConvert(Class<?> sourceType, Class<?> targetType) {return false;}


	/**
	 * 将给定的源转换为指定的目标类型。
	 */
	R convert(Object obj, Class<?> clz) throws TypeMismatchException;

}

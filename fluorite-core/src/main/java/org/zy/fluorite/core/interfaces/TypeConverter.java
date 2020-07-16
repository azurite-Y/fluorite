package org.zy.fluorite.core.interfaces;

import org.zy.fluorite.core.exception.TypeMismatchException;
import org.zy.fluorite.core.subject.ExecutableParameter;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月12日 下午1:24:36;
 * @Description 类型转换器接口
 */ 
public interface TypeConverter {
	
	/**
	 * 将指定对象转换为指定类型
	 * @param <T>
	 * @param value
	 * @param requiredType
	 * @return
	 * @throws TypeMismatchException
	 */
	<T> T convertIfNecessary(Object value, Class<T> requiredType) throws TypeMismatchException;
	
	/**
	 * @param <T>
	 * @param value
	 * @param requiredType
	 * @param executableParameter
	 * @return
	 * @throws TypeMismatchException
	 */
	<T> T convertIfNecessary(Object value, Class<T> requiredType,ExecutableParameter executableParameter) throws TypeMismatchException;
}

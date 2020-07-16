package org.zy.fluorite.core.interfaces;

import org.zy.fluorite.core.subject.AnnotationAttributes;

/**
 * @DateTime 2020年7月4日 上午9:06:21;
 * @author zy(azurite-Y);
 * @Description
 */
public interface ParameterMetadata extends AnnotatedElementMetadate {
	/**
	 * 获得指定参数的类型
	 */
	default Class<?> getType(String parameName) {
		return getAnnotationAttributes(parameName).getElement().getClass();
	}
	/**
	 * 通过参数名获得此参数的AnnotationAttributes对象
	 * @param parameName
	 * @return
	 */
	AnnotationAttributes getAnnotationAttributes(String parameName);
}

package org.zy.fluorite.core.interfaces;

import org.zy.fluorite.core.subject.AnnotationAttributes;

/**
 * @DateTime 2020年7月4日 上午9:54:02;
 * @author zy(azurite-Y);
 * @Description
 */
public interface ExecutableMetadata extends AccessibleObjectMetadate {
	/**
	 * 通过参数名获得此参数的AnnotationAttributes对象
	 * @param parameName
	 * @return
	 */
	default AnnotationAttributes getAnnotationAttributes(String parameName) {return null;}
}

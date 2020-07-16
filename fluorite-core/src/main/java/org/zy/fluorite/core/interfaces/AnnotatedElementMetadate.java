package org.zy.fluorite.core.interfaces;

import java.lang.reflect.AnnotatedElement;

/**
 * @DateTime 2020年6月25日 上午12:24:43;
 * @author zy(azurite-Y);
 * @Description 元数据操作方法顶级接口，定义了适用于类、构造器、方法、属性、参数对象的相关方法
 */
public interface AnnotatedElementMetadate {
	/**
	 * 获得源类型。<br/>
	 * 若实现类表示一组构造器则返回定义此构造器的Class对象。<br/>
	 * 若实现类表示一组方法则返回此方法所在类的类对象。<br/>
	 * 若实现类表示一组属性则返回此属性所在类的类对象<br/>
	 * 若实现类表示一组参数则返回此参数所属的构造器或方法<br/>
	 */
	AnnotatedElement getType();

	/**
	 * 判断指定参数名的参数是否标注了final关键字
	 * @param parameName
	 * @return
	 */
	default boolean isFinal(String parameName) {
		return false;
	}
}

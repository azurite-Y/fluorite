package org.zy.fluorite.core.interfaces;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.zy.fluorite.core.subject.AnnotationAttributes;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午4:46:57;
 * @Description 定义构造器、属性、方法的相关方法 AccessibleObject
 */
public interface AccessibleObjectMetadate extends AnnotatedElementMetadate {
	/**
	 * 是否是可重写的
	 */
	boolean isOverridable();
	
	/**
	 * 返回实例名，若此实现类表示一个类则返回类名，表示一个方法则返回方法的全限定名称
	 */
	String getName();
	
	/**
	 * 是否是抽象的
	 */
	default boolean isAbstract() {
		return false;
	}

	/**
	 * 是否是静态的
	 */
	default boolean isStatic() {
		return false;
	}

	/**
	 * 是否是终态的
	 */
	default boolean isFinal() {
		return false;
	}
	
	AnnotationAttributes getAnnotationAttributes();
	
	/**
	 * 获得所有注解的类型集合
	 * 
	 * @return
	 */
	default Set<Class<? extends Annotation>> getAnnotationTypes() {
		return this.getAnnotationAttributes().keySet();
	}

	/**
	 * 是否拥有指定名称的注解
	 * 
	 * @param annotationName
	 * @return
	 */
	default boolean hasAnnotation(Class<? extends Annotation> annoClz) {
		return this.getAnnotationAttributes().get(annoClz) != null;
	}
}

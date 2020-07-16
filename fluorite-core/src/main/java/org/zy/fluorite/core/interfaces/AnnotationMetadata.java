package org.zy.fluorite.core.interfaces;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.zy.fluorite.core.subject.AnnotationAttributes;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午5:52:55;
 * @Description 操作根源Class注解的相关方法
 */
public interface AnnotationMetadata {
	/**
	 * 确定Class上是否标注有指定类型的注解 
	 * @param clz
	 * @return 返回true则表示被指定类型的注解所标注
	 */
	default boolean isAnnotatedForClass(Class<? extends Annotation> annoClz) {
		return this.getAnnotationForClass(annoClz) != null;
	}

	/**
	 * 确定指定构造器上是否标注有指定类型的注解
	 * 
	 * @param constructor
	 * @param clz
	 * @return 返回true则表示被指定类型的注解所标注
	 */
	default boolean isAnnotatedForConstructor(Constructor<?> constructor, Class<? extends Annotation> annoClz) {
		return this.getAnnotationForConstructor(constructor, annoClz) != null;
	}
	
	/**
	 * 确定指定方法上是否标注有指定类型的注解
	 * 
	 * @param constructor
	 * @param clz
	 * @return 返回true则表示被指定类型的注解所标注
	 */
	default boolean isAnnotatedForMethod(Method method, Class<? extends Annotation> annoClz) {
		return this.getAnnotationForMethod(method, annoClz) != null;
	}
	
	/**
	 * 确定指定属性上是否标注有指定类型的注解
	 * 
	 * @param constructor
	 * @param clz
	 * @return 返回true则表示被指定类型的注解所标注
	 */
	default boolean isAnnotatedForField(Field field, Class<? extends Annotation> annoClz) {
		return this.getAnnotationForField(field, annoClz) != null;
	}
	
	default boolean isAnnotatedForInnerClz(Class<?> innerClz, Class<? extends Annotation> annoClz) {
		return this.getAnnotationForInnerClass(innerClz, annoClz) != null;
	}

	/**
	 * 获得标注于类上的注解映射
	 * @return 未标注注解则返回null
	 */
	AnnotationAttributes getAnnotationAttributesForClass();

	/**
	 * @param method
	 * @return
	 */
	AnnotationAttributes getAnnotationAttributesForConstructor(Constructor<?> constructor);

	/**
	 * 获得标注于指定方法上的注解映射
	 * @return 未标注注解则返回null
	 */
	AnnotationAttributes getAnnotationAttributesForMethod(Method method);

	/**
	 * 获得标注于指定属性上的注解映射
	 * @return 未标注注解则返回null
	 */
	AnnotationAttributes getAnnotationAttributesForField(Field field);

	
	/**
	 * 获得指定内部类上的指定类型的注解（能标注在注解上的注解）
	 * @param <T>
	 * @param class1
	 * @param annoClz
	 * @return
	 */
	default <T>List<T> getAnnotationListForInnerClass(Class<?> innerClz,Class<T> annoClz) {
		AnnotationAttributes attributesForInnerClass = getAnnotationAttributesForInnerClass(innerClz);
		return attributesForInnerClass == null ? null : attributesForInnerClass.getAnnotationList(annoClz);
	}
	
	/**
	 * 获得指定内部类上的指定类型的注解（不能标注在注解上的注解）
	 * @param <T>
	 * @param class1
	 * @param annoClz
	 * @return
	 */
	default <T>T getAnnotationForInnerClass(Class<?> innerClz,Class<T> annoClz) {
		AnnotationAttributes attributesForInnerClass = getAnnotationAttributesForInnerClass(innerClz);
		return (attributesForInnerClass == null ) ? null : attributesForInnerClass.getAnnotation(annoClz);
	}

	/**
	 * 获得指定构造器上的指定类型的注解（不能标注在注解上的注解）
	 * @param <T>
	 * @param constructor
	 * @param clz
	 * @return 未标注注解则返回null
	 */
	default <T> List<T> getAnnotationListForConstructor(Constructor<?> constructor, Class<T> clz) {
		AnnotationAttributes attributesForConstructor = getAnnotationAttributesForConstructor(constructor);
		return attributesForConstructor == null ? null : attributesForConstructor.getAnnotationList(clz);
	}
	
	/**
	 * 获得指定构造器上的指定类型的注解 （能标注在注解上的注解）
	 * @param <T>
	 * @param constructor
	 * @param clz
	 * @return 未标注注解则返回null
	 */
	default <T> T getAnnotationForConstructor(Constructor<?> constructor, Class<T> clz) {
		AnnotationAttributes attributesForConstructor = getAnnotationAttributesForConstructor(constructor);
		return attributesForConstructor == null ? null : attributesForConstructor.getAnnotation(clz);
	}
	
	/**
	 * 获得指定方法上的指定类型的注解 （不能标注在注解上的注解）
	 * @param <T>
	 * @param constructor
	 * @param clz
	 * @return 未标注注解则返回null
	 */
	default <T>T getAnnotationForMethod(Method method, Class<T> clz) {
		AnnotationAttributes attributesForMethod = getAnnotationAttributesForMethod(method);
		return attributesForMethod == null ? null : attributesForMethod.getAnnotation(clz);
	}
	
	/**
	 * 获得指定方法上的指定类型的注解 （能标注在注解上的注解）
	 * @param <T>
	 * @param constructor
	 * @param clz
	 * @return 未标注注解则返回null
	 */
	default <T>List<T> getAnnotationListForMethod(Method method, Class<T> clz) {
		AnnotationAttributes attributesForMethod = getAnnotationAttributesForMethod(method);
		return attributesForMethod == null ? null : attributesForMethod.getAnnotationList(clz);
	}
	
	/**
	 * 获得指定属性上的指定类型的注解
	 * @param <T>
	 * @param constructor
	 * @param clz
	 * @return 未标注注解则返回null
	 */
	default <T> List<T> getAnnotationListForField(Field field, Class<T> clz) {
		AnnotationAttributes attributesForField = getAnnotationAttributesForField(field);
		return attributesForField == null ? null : attributesForField.getAnnotationList(clz);
	}
	
	/**
	 * 获得指定属性上的指定类型的注解 （不能标注在注解上的注解）
	 * @param <T>
	 * @param constructor
	 * @param clz
	 * @return 未标注注解则返回null
	 */
	default <T> T getAnnotationForField(Field field, Class<T> clz) {
		AnnotationAttributes attributesForField = getAnnotationAttributesForField(field);
		return (attributesForField == null) ? null : attributesForField.getAnnotation(clz);
	}
	
	/**
	 * 获得类上的指定类型的注解（不能标注在注解上的注解）
	 * @param <T>
	 * @param constructor
	 * @param clz
	 * @return 未标注注解则返回null
	 */
	default <T> List<T> getAnnotationListForClass(Class<T> clz) {
		AnnotationAttributes attributesForClass = getAnnotationAttributesForClass();
		return attributesForClass == null ? null : attributesForClass.getAnnotationList(clz);
	}
	
	/**
	 * 获得类上的指定类型的注解（能标注在注解上的注解）
	 * @param <T>
	 * @param constructor
	 * @param clz
	 * @return 未标注注解则返回null
	 */
	default <T> T getAnnotationForClass(Class<T> clz) {
		AnnotationAttributes attributesForClass = getAnnotationAttributesForClass();
		return (attributesForClass == null) ? null : attributesForClass.getAnnotation(clz);
	}
	
	/**
	 * 获得指定内部类上的注解映射
	 * @param class1
	 * @return
	 */
	AnnotationAttributes getAnnotationAttributesForInnerClass(Class<?> innerClz);

	/**
	 * 获得此组件的Class对象
	 * @return
	 */
	Class<?> getSourceClass();
	
	/**
	 * 获得标注注解的全部内部类上的注解映射
	 * @return
	 */
	Map<Class<?>, AnnotationMetadata> getAnnotationAttributesForInnerClass();
	
	/**
	 * 获得标注注解的全部属性上的注解映射
	 * @return
	 */
	Map<Field, AccessibleObjectMetadate> getAnnotationAttributesForField();
	
	/**
	 * 获得标注注解的全部方法上的注解映射
	 * @return
	 */
	Map<Method, AccessibleObjectMetadate> getAnnotationAttributesForMethod();
	
	/**
	 * 获得索引标注注解的构造器上的注解映射
	 * @return
	 */
	Map<Constructor<?>, AccessibleObjectMetadate> getAnnotationAttributesForConstructor();
}

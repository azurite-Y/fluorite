package org.zy.fluorite.core.interfaces;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.zy.fluorite.core.subject.AnnotationAttributes;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午9:37:12;
 * @Description 类对象元数据的操作方法的接口
 */
public interface ClassMetadata extends AnnotatedElementMetadate {
	/**
	 * 返回实例名，若此实现类表示一个类则返回类名，表示一个方法则返回方法的全限定名称
	 */
	String getName();
	
	/**
	 * 判断当前对象是否是一个接口
	 */
	boolean isInterface();

	/**
	 * 判断当前对象是否是一个注解
	 */
	boolean isAnnotation();

	/**
	 * 判断当前对象是否是基类，即不是接口或抽象类
	 */
	boolean isConcrete();

	/**
	 * 返回基础类是否有超类
	 */
	boolean hasSuperClass();

	/**
	 * 获得超类的类名
	 */
	String getSuperClassName();

	/**
	 * 获得实现接口的类名
	 */
	String[] getInterfaceNames();

	/**
	 * 返回声明为此ClassMetadata对象表示的类的成员的所有类的名称。
	 * 这包括由类声明的公共、受保护、默认（包）访问和私有类和接口，但不包括继承类和接口。
	 * 如果不存在成员类或接口，则返回空数组。
	 */
	String[] getMemberClassNames();
	
	/**
	 * 获得源类型。<br/>
	 * 若实现类表示一个构造器则返回定义此构造器的Class对象。<br/>
	 * 若实现类表示一个方法则返回此方法的返回值。<br/>
	 * 若实现类表示一个参数或属性则返回其参数类型或属性类型<br/>
	 * 
	 * @return
	 */
	Class<?> getType();

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

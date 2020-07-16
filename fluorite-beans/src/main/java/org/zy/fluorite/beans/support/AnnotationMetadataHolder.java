package org.zy.fluorite.beans.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.zy.fluorite.core.interfaces.AccessibleObjectMetadate;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.AnnotationUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午5:53:32;
 * @Description 保存和解析类信息
 */
public class AnnotationMetadataHolder implements AnnotationMetadata {
	private Map<Constructor<?>, AccessibleObjectMetadate> standardConstructorMetadataMap = new LinkedHashMap<>();
	private Map<Field, AccessibleObjectMetadate> standardFieldMetadataMap = new LinkedHashMap<>();
	private Map<Method, AccessibleObjectMetadate> standardMethodMetadataMap = new LinkedHashMap<>();
	// 存储内部类，支持嵌套的多层内部类
	private Map<Class<?>, AnnotationMetadata> standardInnerClassMetadataMap = new LinkedHashMap<>();

	private StandardClassMetadata standardClassMetadata;
	
	private Class<?> sourceClz;

	public AnnotationMetadataHolder(Class<?> clz) {
		super();
		sourceClz = clz;
		this.standardClassMetadata = new StandardClassMetadata(clz, AnnotationUtils.getAnnotationAttributes(clz));
		parseClz(clz);
	}

	public AnnotationMetadataHolder(Class<?> clz, AnnotationAttributes clzAnnos) {
		super();
		sourceClz = clz;
		standardClassMetadata = new StandardClassMetadata(clz, clzAnnos);
		parseClz(clz);
	}
	
	/**
	 * 解析FactoryBean创建的实例注解信息
	 * @param clz - FactoryBean创建的实例类型
	 * @param clzAnnos - FactoryBean实现标注于类上的注解信息
	 * @param innerClzAnnos - FactoryBean实现标注于内部类上的注解信息
	 */
	public AnnotationMetadataHolder(Class<?> clz, AnnotationAttributes clzAnnos,	Map<Class<?>, AnnotationMetadata> innerClzAnnos) {
		super();
		sourceClz = clz;
		standardClassMetadata = new StandardClassMetadata(clz, clzAnnos);
		standardInnerClassMetadataMap = innerClzAnnos;
		parseClz(clz);
	}

	/**
	 * 解析类对象、属性、构造器、方法
	 * 
	 * @param clz
	 */
	private void parseClz(Class<?> clz) {
		Method[] methodArr = clz.getDeclaredMethods();
		for (Method method : methodArr) {
			try {
				if (method.getAnnotations().length == 0) continue;
			} catch (ArrayStoreException e) {
				System.out.println("asd");
				e.printStackTrace();
			}
			this.standardMethodMetadataMap.put(method, new StandardMethodMetadata(method));
		}

		Field[] fields = clz.getDeclaredFields();
		for (Field field : fields) {
			try {
				if (field.getAnnotations().length == 0) continue;
			} catch (ArrayStoreException e) {
				System.out.println("asd");
				e.printStackTrace();
			}
			this.standardFieldMetadataMap.put(field, new StandardFieldMetadata(field));
		}

		Constructor<?>[] declaredConstructors = clz.getDeclaredConstructors();
		for (Constructor<?> constructor : declaredConstructors) {
			try {
				if (constructor.getAnnotations().length == 0) continue;
			} catch (ArrayStoreException e) {
				System.out.println("asd");
				e.printStackTrace();
			}
			this.standardConstructorMetadataMap.put(constructor, new StandardConstructorMetadata(constructor));
		}
		
//		if (!this.standardInnerClassMetadataMap.isEmpty()) { // 已填充了属性就不在解析
//			return ;
//		}
		
		// 获得当前Class对象的内部类
		Class<?>[] declaredClasses = clz.getDeclaredClasses();
		for (Class<?> innerClz : declaredClasses) {
			try {
				if (innerClz.getAnnotations().length == 0) continue;
			} catch (ArrayStoreException e) {
				System.out.println("asd");
				e.printStackTrace();
			}
			this.standardInnerClassMetadataMap.put(innerClz, new AnnotationMetadataHolder(innerClz));
		}
		
		// 解析父类，如果有的话
//		Class<?> superclass = clz.getSuperclass();
//		if (superclass != null && superclass != Object.class) {
//			analysisClz(clz);
//		}
	}

	@Override
	public Class<?> getSourceClass() {
		return this.sourceClz;
	}

	@Override
	public AnnotationAttributes getAnnotationAttributesForClass() {
		return this.standardClassMetadata.getAnnotationAttributes();
	}

	@Override
	public AnnotationAttributes getAnnotationAttributesForMethod(Method method) {
		AccessibleObjectMetadate standardMethodMetadata = this.standardMethodMetadataMap.get(method);
		return standardMethodMetadata == null ? null : standardMethodMetadata.getAnnotationAttributes();
	}

	@Override
	public AnnotationAttributes getAnnotationAttributesForField(Field field) {
		AccessibleObjectMetadate fieldMetadata = this.standardFieldMetadataMap.get(field);
		return fieldMetadata == null ? null : fieldMetadata.getAnnotationAttributes();
	}

	@Override
	public AnnotationAttributes getAnnotationAttributesForConstructor(Constructor<?> constructor) {
		AccessibleObjectMetadate constructorMetadata = this.standardConstructorMetadataMap.get(constructor);
		return constructorMetadata == null ? null : constructorMetadata.getAnnotationAttributes();
	}

	@Override
	public AnnotationAttributes getAnnotationAttributesForInnerClass(Class<?> innerClz) {
		AnnotationMetadata annotationMetadata = this.standardInnerClassMetadataMap.get(innerClz);
		return annotationMetadata==null ? null : annotationMetadata.getAnnotationAttributesForClass();
	}

	@Override
	public Map<Class<?>, AnnotationMetadata> getAnnotationAttributesForInnerClass() {
		return this.standardInnerClassMetadataMap;
	}

	@Override
	public Map<Field, AccessibleObjectMetadate> getAnnotationAttributesForField() {
		return this.standardFieldMetadataMap;
	}

	@Override
	public Map<Method, AccessibleObjectMetadate> getAnnotationAttributesForMethod() {
		return this.standardMethodMetadataMap;
	}

	@Override
	public Map<Constructor<?>, AccessibleObjectMetadate> getAnnotationAttributesForConstructor() {
		return this.standardConstructorMetadataMap;
	}
}

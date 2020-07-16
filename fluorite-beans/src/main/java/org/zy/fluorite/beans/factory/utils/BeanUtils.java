package org.zy.fluorite.beans.factory.utils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Priority;

import org.zy.fluorite.beans.factory.exception.BeanInstantiationException;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.annotation.DependsOn;
import org.zy.fluorite.core.annotation.Description;
import org.zy.fluorite.core.annotation.Lazy;
import org.zy.fluorite.core.annotation.Primary;
import org.zy.fluorite.core.annotation.Qualifier;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午10:15:59;
 * @Description
 */
public class BeanUtils {

	/**
	 * 获得指定方法的PropertyDescriptor对象
	 * @param method
	 * @param clz
	 * @return
	 */
	public static PropertyDescriptor getPropertyDescriptor(Field field,Class<?> clz) {
		return getPropertyDescriptor(field.getName(), clz);
	}
	
	/**
	 * 通过指定的名称生成PropertyDescriptor
	 * @param name
	 * @param clz
	 * @return
	 */
	public static PropertyDescriptor getPropertyDescriptor(String name,Class<?> clz) {
		try {
			return new PropertyDescriptor(name, clz);
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 获得指定方法的PropertyDescriptor对象
	 * @param method - 标准的setter
	 * @param clz
	 * @return
	 */
	public static PropertyDescriptor getPropertyDescriptor(Method method,Class<?> clz) {
		String name = method.getName();
		
		Assert.isTrue(!name.startsWith("set"), () -> "不合java规范的PropertyDescriptor源对象,方法需为标准的setter方法,by method" + method );

//		String charAt = name.charAt(3) + "";
//		String lowerCase = charAt.toLowerCase();
//		return getPropertyDescriptor(lowerCase, clz);
		
		/**
		 *  在创建字符串时自前开始截除3个字符
		 *  在调用PropertyDescriptor的构造器时，会自动将name属性的开头首字母小写，所以此处不进行首字母小写操作
		 */
		return getPropertyDescriptor(new String(name.toCharArray(),3,name.length()-3), clz);
	}
	
	/**
	 * 获得指定类所有属性的PropertyDescriptor对象
	 * bug：在属性名为一个字符的时候会触发空指针异常
	 * @param clazz
	 * @return
	 */
	public static List<PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) {
		final List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();
		ReflectionUtils.doWithLocalFields(clazz, field -> {
			propertyDescriptors.add( getPropertyDescriptor(field, clazz) );
		});
		return propertyDescriptors;
	}
	
	public static <T> T instantiateClass(Constructor<T> ctor, Object... args) throws BeanInstantiationException {
		Assert.notNull(ctor,"反射调用的构造器对象不可为null");
		try {
			return ctor.newInstance(args);
		}catch (InstantiationException ex) {
			throw new BeanInstantiationException("声明基础构造函数的类为抽象类，by："+ctor.getDeclaringClass(), ex);
		}	catch (IllegalAccessException ex) {
			throw new BeanInstantiationException( "非public的构造器函数对象，访问受限，by："+ctor, ex);
		}	catch (IllegalArgumentException ex) {
			throw new BeanInstantiationException("构造器实际参数与形参数目或类型不符，by：" + ctor + " ，形参："+Arrays.asList(args), ex)  ;
		}	catch (InvocationTargetException ex) {
			throw new BeanInstantiationException("构造器抛出异常，by：" + ctor, ex.getTargetException());
		}
	}
	
	/**
	 * 解析注解(标注于类的注解)填充beanDefinition相关属性
	 * 
	 * @param sourceClass
	 * @Lazy - lazyInit
	 * @Primary – primary
	 * @Priority - priority
	 * @DependsOn – dependsOn
	 * @Description - description
	 * @Qualifier - qualifiedName
	 */
	public static void processCommonDefinitionAnnotations(BeanDefinition beanDefinition, AnnotationMetadata metadata) {
		Assert.notNull(beanDefinition, "'beanDefinition'不可为null");
		Assert.notNull(metadata, "‘metadata’不可为null");
		
		Lazy lazy = metadata.getAnnotationForClass(Lazy.class);
		if (lazy != null && lazy.value())
			beanDefinition.setLazyInit(true);

		Primary primary = metadata.getAnnotationForClass(Primary.class);
		if (primary != null)
			beanDefinition.setPrimary(true);

		Priority priority = metadata.getAnnotationForClass(Priority.class);
		if (priority != null)
			beanDefinition.setPriority(priority.value());

		DependsOn dependsOn = metadata.getAnnotationForClass(DependsOn.class);
		if (primary != null)
			beanDefinition.setDependsOn(dependsOn.value());

		Description description = metadata.getAnnotationForClass(Description.class);
		if (primary != null)
			beanDefinition.setDescription(description.value());

		Qualifier qualifier = metadata.getAnnotationForClass(Qualifier.class);
		if (primary != null)
			beanDefinition.setQualifiedName(qualifier.value());
	}
}

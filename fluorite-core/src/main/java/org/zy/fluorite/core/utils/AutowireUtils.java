package org.zy.fluorite.core.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.zy.fluorite.core.interfaces.function.ObjectFactory;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午4:43:52;
 * @Description 自动注入工具类
 */
public class AutowireUtils {
	public static final int AUTOWIRE_NO = 0;
	public static final int AUTOWIRE_BY_NAME = 1;
	public static final int AUTOWIRE_BY_TYPE = 2;
	public static final int AUTOWIRE_CONSTRUCTOR = 3;
	
	/** com.zy.fluorite.beans.annotation.Component */
	public static final String COMPONENT = "com.zy.fluorite.beans.annotation.Component";
	/** javax.annotation.ManagedBean */
	public static final String MANAGEBEAN = "javax.annotation.ManagedBean";
	/** javax.inject.Named */
	public static final String NAMED = "javax.inject.Named";
	/** org.zy.fluorite.core.annotation.Qualifier */
	public static final String QUALIFIER = "org.zy.fluorite.core.annotation.Qualifier";
	
	/** 若返回负整数则代表从小到大的升序排列，若返回正整数则代表从大到小的降序排列 */
	public static final Comparator<Executable> EXECUTABLE_COMPARATOR = (e1, e2) -> {
		/**
		 *  Boolean.compare - return (x == y) ? 0 : (x ? 1 : -1);
		 *  比较两参数方法是否被public修饰符修饰，若两个均被修饰或未被修饰则Boolean.compare返回值为0
		 *  Integer.compare - return (x < y) ? -1 : ((x == y) ? 0 : 1);
		 */
		int result = Boolean.compare(Modifier.isPublic(e2.getModifiers()), Modifier.isPublic(e1.getModifiers()));
		return result != 0 ? result : Integer.compare(e2.getParameterCount(), e1.getParameterCount());
	};

	/**
	 * 候选构造器排序，公共构造器优先于非公共构造器，参数个数小的优先于参数个数大的
	 * @param candidates
	 */
	public static void sortConstructors(Constructor<?>[] candidates) {
		Arrays.sort(candidates, EXECUTABLE_COMPARATOR);
	}
	/**
	 * 候选构造器排序，公共构造器优先于非公共构造器，参数个数小的优先于参数个数大的
	 * @param candidates
	 */
	public static void sortConstructors(List<Constructor<?>> candidates) {
		sortConstructors( 
				candidates.toArray(new Constructor<?>[0]) );
	}
	
	/**
	 * 获选工厂方法排序，公共方法优先于非公共方法，参数个数小的优先于参数个数大的
	 * @param factoryMethods
	 */
	public static void sortFactoryMethods(Method[] factoryMethods) {
		Arrays.sort(factoryMethods, EXECUTABLE_COMPARATOR);
	}
	/**
	 * 获选工厂方法排序，公共方法优先于非公共方法，参数个数小的优先于参数个数大的
	 * @param factoryMethods
	 */
	public static void sortFactoryMethods(List<Method> factoryMethods) {
		sortFactoryMethods(
				factoryMethods.toArray(new Method[0]) );
	}
	
	/**
	 * 将自动装配对象解析为需要的类型
	 * @param autowiringValue
	 * @param requiredType
	 * @return
	 */
	public static Object resolveAutowiringValue(Object autowiringValue, Class<?> requiredType) {
		if (autowiringValue instanceof ObjectFactory && !requiredType.isInstance(autowiringValue)) {
			ObjectFactory<?> factory = (ObjectFactory<?>) autowiringValue;
			return factory.getObject();
		} 
		return autowiringValue;
	}
}

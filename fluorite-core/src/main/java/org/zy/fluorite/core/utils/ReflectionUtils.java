package org.zy.fluorite.core.utils;

import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.zy.fluorite.core.interfaces.function.ForEachCallback;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 上午9:26:32;
 * @Description 反射工具类
 */
public class ReflectionUtils {
	private static final Map<Class<?>,Object> instantiateCache = new ConcurrentHashMap<>();
	private static final Map<String,Class<?>> classCache = new ConcurrentHashMap<>();

	// 数组容器转换标识
	public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	public static final Constructor<?>[] EMPTY_CONSTRUCTOR_ARRAY = new Constructor<?>[0];

	public static final Method[] EMPTY_METHOD_ARRAY = new Method[0];

	public static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	public static final String GET_PREFIX = "get";
	public static final String SET_PREFIX = "set";
	public static final String IS_PREFIX = "is";

	/** 过滤桥接方法和编译器引入的方法 */
	public static final MatcherFilter<Method> USER_DECLARED_METHODS =	(method -> !method.isBridge() && !method.isSynthetic());

	/** 过滤编译器引入的属性 */
	public static final MatcherFilter<Field> USER_DECLARED_FFIELD =	(field -> !field.isSynthetic());

	/**
	 * 反射创建指定值的Class对象
	 * 
	 * @param className
	 * @return
	 */
	public static Class<?> forName(String className) {
		Class<?> clz = classCache.get(className);
		if (clz != null) return clz;
		try {
			Class<?> forName = Class.forName(className);
			classCache.put(className, forName);
			return forName;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 通过Class对象获得其实例
	 * @param <T>
	 * @param clz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T>T instantiateClass(Class<T> clz){
		Object object = instantiateCache.get(clz);
		if (object != null) {
			return (T) object;
		}
		try {
			T newInstance = clz.newInstance();
			instantiateCache.put(clz, newInstance);
			return newInstance;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 使给定方法或构造器可访问，必要时显式设置为可访问。
	 */
	public static void makeAccessible(Executable executable) {
		if ((!Modifier.isPublic(executable.getModifiers()) || !Modifier.isPublic(executable.getDeclaringClass().getModifiers()))
				&& !executable.isAccessible()) {
			executable.setAccessible(true);
		}
	}
	/**
	 * 使给定属性可访问，必要时显式设置为可访问。
	 */
	public static void makeAccessible(Field field) {
		if ((!Modifier.isPublic(field.getModifiers()) ||
				!Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
				Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
			field.setAccessible(true);
		}
	}

	/**
	 * 迭代指定Class所定义的全部Method对象，且使用默认的方法过滤逻辑
	 * @param clz
	 * @param callback
	 */
	public static void doWithLocalMethods(Class<?> clz, ForEachCallback<Method> callback) {
		doWithLocalMethods(clz,callback,USER_DECLARED_METHODS);
	}

	/**
	 * 迭代指定Class所定义的全部Method对象，且指定方法过滤逻辑
	 * @param clz
	 * @param callback
	 */
	public static void doWithLocalMethods(Class<?> clz, ForEachCallback<Method> callback,MatcherFilter<Method> matcher) {
		Method[] methods = clz.getDeclaredMethods();
		for (Method method : methods) {
			if (matcher != null && !matcher.matches(method)) {
				continue ;
			}
			callback.action(method);
		}
	}

	/**
	 * 迭代指定类所定义的全部Field对象，且使用默认的方法过滤逻辑
	 * @param clz
	 * @param callback
	 */
	public static void doWithLocalFields(Class<?> clz,  ForEachCallback<Field> callback) {
		doWithLocalFields(clz,callback ,USER_DECLARED_FFIELD);
	}

	/**
	 * 迭代指定类所定义的全部Field对象
	 * @param clz
	 * @param callback
	 */
	public static void doWithLocalFields(Class<?> clz,  ForEachCallback<Field> callback , MatcherFilter<Field> matcher) {
		Field[] declaredFields = clz.getDeclaredFields();
		for (Field field : declaredFields) {
			if (matcher != null && !matcher.matches(field)) {
				continue ;
			}
			callback.action(field);
		}
	}

	/**
	 * 迭代指定Class所定义的全部Constructor对象
	 * @param clz
	 * @param callback
	 */
	public static void doWithLocalConstructor(Class<?> clz,  ForEachCallback<Constructor<?>> callback) {
		Constructor<?>[] declaredConstructors = clz.getDeclaredConstructors();
		for (Constructor<?> constructor : declaredConstructors) {
			callback.action(constructor);
		}
	}

	public static Object invokeMethod(Object obj, Method method ,Object... args) {
		try {
			makeAccessible(method);
			return method.invoke(obj,args);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	/**
	 * 通过Class对象获得其实例
	 * @param <T>
	 * @param clz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T>T instantiateClass(Class<T> clz,Object[] args){
		try {
			if (Assert.notNull(args)) {
				Constructor<?>[] declaredConstructors = clz.getDeclaredConstructors();
				for (Constructor<?> constructor : declaredConstructors) {
					if (constructor.getParameterCount() == args.length) {
						try {
							return (T)constructor.newInstance(args);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return clz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 批量生成Class对象
	 * @param importClassNames
	 * @return
	 */
	public static Set<Class<?>> asClasses(String[] classNames) {
		Set<Class<?>> set = new LinkedHashSet<>();
		for (String im : classNames) {
			Class<?> forName = ReflectionUtils.forName(im);
			set.add(forName);
		}
		return set;
	}

	/**
	 * 批量生成Class对象
	 * @param importClassNames
	 * @return
	 */
	public static Set<Class<?>> asClasses(Collection<String> search) {
		Set<Class<?>> set = new LinkedHashSet<>();
		for (String im : search) {
			Class<?> forName = ReflectionUtils.forName(im);
			set.add(forName);
		}
		return set;
	}

	@FunctionalInterface
	public interface MatcherFilter<T> {

		/** 在此定义过滤逻辑 */
		boolean matches(T t);
	}

	/**
	 * 获取给定类和参数的可访问构造函数
	 * @param <T>
	 * @param clazz
	 * @param parameterTypes
	 * @return
	 * @throws NoSuchMethodException
	 */
	public static <T> Constructor<T> accessibleConstructor(Class<T> clazz, Class<?>... parameterTypes)
			throws NoSuchMethodException {
		Constructor<T> ctor = clazz.getDeclaredConstructor(parameterTypes);
		makeAccessible(ctor);
		return ctor;
	}

	/** 获得指定类所定义的方法并存储于List容器中，使用默认的方法过滤逻辑 */
	public static List<Method> doWithLocalMethodsToList(Class<?> clz) {
		List<Method> list = new ArrayList<>();
		doWithLocalMethods(clz, list::add);
		return list;
	}

	/** 获得指定类所定义的方法并存储于Method数组中，使用默认的方法过滤逻辑 */
	public static Method[] doWithLocalMethodsToArray(Class<?> clz) {
		List<Method> localMethodsToList = doWithLocalMethodsToList(clz);
		return localMethodsToList.toArray(EMPTY_METHOD_ARRAY);
	}

	/**
	 * 获得getter方法，boolean 属性的getter方法名为 isXxxx
	 * @return 
	 * @throws IntrospectionException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@SuppressWarnings("all")
	public static Method findGetterMethod(String propertyName, Class<?> beanClass,Class<?> propretyClass) throws IntrospectionException, NoSuchMethodException, SecurityException {
		String prefix =  Boolean.class.isAssignableFrom(propretyClass) || boolean.class.isAssignableFrom(propretyClass)  ? IS_PREFIX : GET_PREFIX;
		String capitalize = StringUtils.capitalize(propertyName);
		return beanClass.getMethod(prefix + capitalize, null);
	}

	/**
	 * 获得setter方法
	 * @param propertyName
	 * @param beanClass
	 * @param propretyClass
	 * @return
	 * @throws IntrospectionException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@SuppressWarnings("all")
	public static Method findsetterMethod(String propertyName, Class<?> beanClass,Class<?> propretyClass) throws IntrospectionException, NoSuchMethodException, SecurityException {
		String capitalize = StringUtils.capitalize(propertyName);
		return beanClass.getMethod(SET_PREFIX + capitalize, null);
	}
	
	public static void clearCache() {
		instantiateCache.clear();
		classCache.clear();
	}
}

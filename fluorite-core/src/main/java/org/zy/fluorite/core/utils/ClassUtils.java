package org.zy.fluorite.core.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午5:44:11;
 * @Description 类对象操作工具类
 */
public class ClassUtils {
	/** 通过Cglib生成的子类所拥有的分隔符 */
	public static final String CGLIB_CLASS_SEPARATOR = "&&";

	/**
	 * 检查给定的类是否与用户指定的类型名匹配
	 * 
	 * @param clazz
	 * @param typeName
	 * @return
	 */
	public static boolean matchesTypeName(Class<?> clazz, String typeName) {
		return (typeName != null && (typeName.equals(clazz.getTypeName()) || typeName.equals(clazz.getSimpleName())));
	}

	public static int getMethodCountForName(Class<?> clz, String methodName) {
		Assert.notNull(clz, "Class对象不能为null");
		Assert.notNull(methodName, "方法名称不能为null");
		int count = 0;
		Method[] declaredMethods = clz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (methodName.equals(method.getName())) {
				count++;
			}
		}
		Class<?>[] ifcs = clz.getInterfaces();
		for (Class<?> ifc : ifcs) {
			count += getMethodCountForName(ifc, methodName);
		}
		if (clz.getSuperclass() != null) {
			count += getMethodCountForName(clz.getSuperclass(), methodName);
		}
		return count;
	}

	/**
	 * 返回给定方法的限定名，由完全限定的接口/类名+“”+方法名组成
	 * 
	 * @param method
	 * @return
	 */
	public static String getQualifiedMethodName(Method method) {
		return getQualifiedMethodName(method, null);
	}

	/**
	 * 返回给定方法的限定名，由完全限定的接口/类名+“”+方法名组成
	 * 
	 * @param method
	 * @param clazz
	 * @return
	 */
	public static String getQualifiedMethodName(Method method, Class<?> clazz) {
		Assert.notNull(method, "Method不能为null.");
		return (clazz != null ? clazz : method.getDeclaringClass()).getName() + '.' + method.getName();
	}

	public static boolean isInnerClass(Class<?> clz) {
		return (clz.isMemberClass() && !Modifier.isStatic(clz.getModifiers()));
	}

	/**
	 * 返回类加载器实现
	 * 
	 * @return
	 */
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		return loader == null ? ClassUtils.class.getClassLoader() : loader;
	}

	/**
	 * 若指定类对象是Cglib生成的子类则获取其父类的Class对象，反之则直接返回此类对象
	 * 
	 * @param clz
	 * @return
	 */
	public static Class<?> getUserClass(Class<?> clz) {
		if (clz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
			Class<?> superclass = clz.getSuperclass();
			if (superclass != null && superclass != Object.class) {
				return superclass;
			}
		}
		return clz;
	}

	/**
	 * 若指定对象是Cglib生成的子类则获取其父类的Class对象，反之则直接返回此类的Class对象
	 * 
	 * @param clz
	 * @return
	 */
	public static Class<?> getUserClass(Object obj) {
		return getUserClass(obj.getClass());
	}

	/**
	 * 判断给定的类是否由指定的构造器进行加载，是则返回true
	 * @param clz
	 * @param classLoader
	 * @return
	 */
	public static boolean isCacheSafe(Class<?> clz, ClassLoader classLoader) {
		Assert.notNull(clz, "Class不能为null");

		try {
			ClassLoader target = clz.getClassLoader();
			if (target == classLoader || target == null) {
				return true;
			}
			if (classLoader == null) {
				return false;
			}
			// 检查指定类加载器的双亲
			ClassLoader current = classLoader;
			while (current != null) {
				current = current.getParent();
				if (current == target) {
					return true;
				}
			}
		} catch (SecurityException ex) {}
		return false;
	}

	public static Class<?>[] toClassArray(Collection<Class<?>> collection) {
		return (Assert.notNull(collection) ? collection.toArray(ReflectionUtils.EMPTY_CLASS_ARRAY) : ReflectionUtils.EMPTY_CLASS_ARRAY);
	}

	/**
	 * 获得指定对象实现的所有接口
	 * @param target
	 * @return
	 */
	public static Class<?>[] getAllInterfaces(Object target) {
		Set<Class<?>> interfaces = getAllInterfacesToSet(target.getClass());
		return interfaces.toArray(new Class<?>[]{});
	}
	
	/**
	 * 获得指定对象实现的所有接口
	 * @param target
	 * @return
	 */
	public static Set<Class<?>> getAllInterfacesToSet(Class<?> targetClz) {
		Set<Class<?>> interfaces = new LinkedHashSet<>();
		Class<?> current = targetClz;
		while (current != null && current != Object.class) {
			Class<?>[] ifcs = current.getInterfaces();
			for (Class<?> ifc : ifcs) {
				interfaces.add(ifc);
			}
			current = current.getSuperclass();
		}
		return interfaces;
	}
}

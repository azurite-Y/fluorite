package org.zy.fluorite.core.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @DateTime 2021年10月10日;
 * @author zy(azurite-Y);
 * @Description
 */
public class TypeUtils {
	public final static List<Class<?>> basicTypeList = new ArrayList<>();

	static {
		basicTypeList.add(byte.class);
		basicTypeList.add(Byte.class);
		basicTypeList.add(short.class);
		basicTypeList.add(Short.class);
		basicTypeList.add(int.class);
		basicTypeList.add(Integer.class);
		basicTypeList.add(double.class);
		basicTypeList.add(Double.class);
		basicTypeList.add(float.class);
		basicTypeList.add(Float.class);
		basicTypeList.add(long.class);
		basicTypeList.add(Long.class);
		basicTypeList.add(boolean.class);
		basicTypeList.add(Boolean.class);
		basicTypeList.add(char.class);
		basicTypeList.add(Character.class);
	}
	
	/**
	 * 判断指定的类型是否是 基本数据类型及其封装类
	 * @param checkClz
	 * @return
	 */
	public static boolean isBasicType(Class<?> checkClz) {
		return basicTypeList.contains(checkClz);
	}

	/**
	 * 判断指定的类型是否是 基本数据类型及其封装、String类型
	 * @param checkClz
	 * @return
	 */
	public static boolean isDefaultType(Class<?> checkClz) {
		return String.class.isAssignableFrom(checkClz) || basicTypeList.contains(checkClz);
	}
}

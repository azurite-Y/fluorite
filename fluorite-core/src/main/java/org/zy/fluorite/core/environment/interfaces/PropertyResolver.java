package org.zy.fluorite.core.environment.interfaces;

import java.util.List;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月16日 下午3:17:05;
 * @Description 属性源解析器顶级接口，定义属性解析的相关方法
 */
public interface PropertyResolver {
	/**
	 * 判断属性源中是否拥有对应键名的属性值，若有则返回true
	 */
	boolean containsProperty(String key);

	/**
	 * 从当前属性源中获得此键名对应的属性值，若没有指定的键名则返回null
	 */
	String getProperty(String key);

	/**
	 * 从当前属性源中获得此键名对应的属性值，若没有指定的键名则返回默认值
	 */
	String getProperty(String key, String defaultValue);

	/**
	 * 从当前属性源中获得此键名对应的属性值。
	 * 若没有指定的键名则返回null，有则返回指定类型的属性值
	 */
	<T> T getProperty(String key, Class<T> targetType);

	/**
	 * 从当前属性源中获得此键名对应的属性值。
	 * 若没有指定的键名则返回默认值，有则返回指定类型的属性值
	 */
	<T> T getProperty(String key, Class<T> targetType, T defaultValue);

	/**
	 * 返回与给定键关联的属性值，若属性值为null则抛出异常
	 */
	String getRequiredProperty(String key) throws IllegalStateException;

	/**
	 * 指定必须存在哪些属性
	 * @param requiredProperties
	 */
	void setRequiredProperties(String... requiredProperties);
	
	/**
	 * 验证setRequiredProperties指定的每个属性是否存在，
	 * 不存在则触发MissingRequiredPropertiesException异常
	 */
	void validateRequiredProperties();
	
	/**
	 * 从当前属性源中获得此键名对应的属性值。
	 * 若没有指定的键名则抛出异常，有则返回指定类型的属性值
	 */
	<T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException;

	/**
	 * 解析给定文本中的${…}占位符，用getProperty解析的相应属性值替换它们。
	 * 不带默认值的不可解析占位符将被忽略并以未更改的方式返回
	 */
	String resolvePlaceholders(String text);

	/**
	 * 解析给定文本中的${…}占位符，用getProperty解析的相应属性值替换它们。
	 * 没有默认值的不可解析占位符将导致引发IllegalArgumentException
	 */
	String resolveRequiredPlaceholders(String text) throws IllegalArgumentException;

	List<String> getPropertyToList(String key);
}

package org.zy.fluorite.core.environment;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.core.environment.interfaces.PropertySource;
import org.zy.fluorite.core.environment.interfaces.PropertySources;
import org.zy.fluorite.core.exception.MissingRequiredPropertiesException;
import org.zy.fluorite.core.interfaces.ConversionServiceStrategy;
import org.zy.fluorite.core.interfaces.function.ActiveFunction;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年6月16日 下午4:30:16;
 * @author zy(azurite-Y);
 * @Description 存储多个属性源对象并提供存取之外的插入功能
 */
public class MutablePropertySources implements PropertySources {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final List<PropertySource<?>> propertySources = new CopyOnWriteArrayList<>();

	private final Set<String> requiredProperties = new LinkedHashSet<>();

	private ConversionServiceStrategy conversionServiceStrategy;
	
	public MutablePropertySources(ConversionServiceStrategy conversionServiceStrategy) {
		this.conversionServiceStrategy = conversionServiceStrategy;
	}

	public MutablePropertySources(PropertySources propertySources) {
		this(propertySources.getConversionServiceStrategy());
		for (PropertySource<?> propertySource : propertySources) {
			addLast(propertySource);
		}
	}

	@Override
	public Iterator<PropertySource<?>> iterator() {
		return this.propertySources.iterator();
	}

	@Override
	public Spliterator<PropertySource<?>> spliterator() {
		return Spliterators.spliterator(this.propertySources, 0);
	}

	@Override
	public Stream<PropertySource<?>> stream() {
		return this.propertySources.stream();
	}

	@Override
	public boolean contains(String name) {
		for (PropertySource<?> propertySource : propertySources) {
			if (propertySource.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 自定义匹配规则的获得PropertySource对象
	 * 
	 * @param list
	 * @param active
	 * @return
	 */
	public PropertySource<?> forEach(List<PropertySource<?>> list, ActiveFunction<Boolean, PropertySource<?>> active) {
		for (PropertySource<?> propertySource : list) {
			try {
				if (active.active(propertySource)) {
					return propertySource;
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public PropertySource<?> get(String name) {
		return assertGetElement(name);
	}

	/**
	 * 添加最高优先级的属性源包装对象
	 */
	public void addFirst(PropertySource<?> propertySource) {
		removeIfPresent(propertySource);
		this.propertySources.add(0, propertySource);
	}

	/**
	 * 添加最低优先级的属性源包装对象
	 */
	public void addLast(PropertySource<?> propertySource) {
		removeIfPresent(propertySource);
		this.propertySources.add(propertySource);
	}

	private boolean removeIfPresent(PropertySource<?> propertySource) {
		return this.propertySources.remove(propertySource);
	}

	private void addAtIndex(int index, PropertySource<?> propertySource) {
		this.propertySources.add(index, propertySource);
	}

	private PropertySource<?> assertGetElement(String name) {
		PropertySource<?> source = this.forEach(propertySources, ps -> {
			return ps.getName().equals(name);
		});
		Assert.notNull(source, "未从数据源中找到指定名称的PropertySource对象，by name：" + name);
		return source;
	}

	private int foundIndex(String name) {
		for (int i = 0; i < this.propertySources.size(); i++) {
			PropertySource<?> propertySource = this.propertySources.get(i);
			if (propertySource.getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 将属性源对象添加到指定名称的属性源对象之后
	 */
	public void addAfter(String relativePropertySourceName, PropertySource<?> propertySource) {
		int index = foundIndex(relativePropertySourceName);
		removeIfPresent(propertySource);
		addAtIndex(index + 1, propertySource);
	}

	/**
	 * 将属性源对象添加到指定名称的属性源对象之前
	 */
	public void addBefore(String relativePropertySourceName, PropertySource<?> propertySource) {
		int index = foundIndex(relativePropertySourceName);
		removeIfPresent(propertySource);
		addAtIndex(index, propertySource);
	}

	/**
	 * 返回指定属性源的优先级
	 */
	public int precedenceOf(PropertySource<?> propertySource) {
		return this.foundIndex(propertySource.getName());
	}

	/**
	 * 替换指定位置的PropertySource
	 */
	public void replace(String name, PropertySource<?> propertySource) {
		int index = foundIndex(name);
		this.propertySources.set(index, propertySource);
	}

	/**
	 * 返回属性源集合长度
	 * 
	 * @return
	 */
	public int size() {
		return this.propertySources.size();
	}

	@Override
	public String toString() {
		return this.propertySources.toString();
	}

	/**
	 * 获得指定名称和类型的属性值，
	 * 
	 * @param key
	 * @param clz
	 * @param resolveNestedPlaceholders
	 * @return
	 */
	protected <T> T getProperty(String key, Class<T> clz, boolean resolveNestedPlaceholders) {
		if (!this.propertySources.isEmpty()) {
			for (PropertySource<?> propertySource : this.propertySources) {
				Object value = propertySource.getProperty(key);
				if (value != null) {
					if (resolveNestedPlaceholders && value instanceof String) {
						value = resolveNestedPlaceholders((String) value);
					}
					logKeyFound(key, propertySource, value);
					return convertValueIfNecessary(value, clz);
				}
			}
		}
//		if (DebugUtils.debug) {
//			logger.info("未找到指定名称的属性源对象，by key：‘" + key+"'");
//		}
		return null;
	}

	/**
	 * 占位符解析（未完成）
	 * 
	 * @param value
	 * @return
	 */
	private String resolveNestedPlaceholders(String value) {
		return value;
	}

	/**
	 * 类型转换
	 * 
	 * @param value
	 * @param clz
	 * @return
	 * @throws Exception 
	 */
	private <T> T convertValueIfNecessary(Object value, Class<T> clz) {
		return (T) conversionServiceStrategy.convert(value, clz);
	}

	private void logKeyFound(String key, PropertySource<?> propertySource, Object value) {
//		if (DebugUtils.debug) {
//			logger.info("已找到指定名称的属性源对象，by key：'" + key + "'='" +value+"'，属性源名称："+ propertySource.getName());
//		}
	}

	public String getProperty(String key) {
		return getProperty(key, String.class, true);
	}

	public List<String> getPropertyToList(String name) {
		List<String> propertyToList = null;
		if (this.propertySources != null) {
			for (PropertySource<?> propertySource : this.propertySources) {
				propertyToList = propertySource.getPropertyToList(name);
				if (Assert.notNull(propertyToList)) {
					return propertyToList;
				}
			}
		}
		return propertyToList;
	}

	public boolean containsProperty(String key) {
		if (this.propertySources != null) {
			for (PropertySource<?> propertySource : this.propertySources) {
				if (propertySource.containsProperty(key)) {
					return true;
				}
			}
		}
		return false;
	}

	public String getProperty(String key, String defaultValue) {
		String property = getProperty(key, String.class, true);
		return Assert.hasText(property) ? property : defaultValue;
	}

	public <T> T getProperty(String key, Class<T> targetType) {
		return getProperty(key, targetType, true);
	}

	public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
		T property = getProperty(key, targetType, true);
		return Assert.notNull(property) ? property : defaultValue;
	}

	public String getRequiredProperty(String key) {
		String value = getProperty(key);
		Assert.notNull(value, "缺少必须存在的键，by：" + key);
		return value;
	}

	public <T> T getRequiredProperty(String key, Class<T> targetType) {
		T value = getProperty(key, targetType);
		Assert.notNull(value, "缺少必须存在的键，by：" + key);
		return value;
	}
	
	public void setRequiredProperties(String[] requiredProperties) {
		Collections.addAll(this.requiredProperties, requiredProperties);
	}

	public void validateRequiredProperties() {
		for (String key : this.requiredProperties) {
			if (this.getProperty(key) == null) {
				new MissingRequiredPropertiesException("缺少必要的属性键，by：" + key).printStackTrace();
			}
		}
	}

	@Override
	public ConversionServiceStrategy getConversionServiceStrategy() {
		return this.conversionServiceStrategy;
	}
}

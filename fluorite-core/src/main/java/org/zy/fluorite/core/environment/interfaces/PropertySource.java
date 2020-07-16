package org.zy.fluorite.core.environment.interfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年6月16日 下午4:30:55;
 * @author zy(azurite-Y);
 * @Description
 */
public abstract class PropertySource<T> {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected final String name;

	protected final T source;
	
	public PropertySource(String name, T source) {
		super();
		Assert.hasText(name, "属性源名称不能为null或空串");
		Assert.notNull(source, "属性源对象不能为null");
		
		this.name = name;
		this.source = source;
	}
	
	public String[] getPropertyName() {
		return null;
	}

	/**
	 * 创建最低优先级的属性源对象包装类
	 * @param name
	 */
	@SuppressWarnings("unchecked")
	public PropertySource(String name) {
		this(name, (T) new Object());
	}
	
	/**
	 * 获得属性源名称
	 * @return
	 */
	public String getName() {
		return this.name;
	}


	/**
	 * 获得当前对象包装的属性源对象
	 */
	public T getSource() {
		return this.source;
	}

	/**
	 * 判断属性源中是否拥有对应键名的属性值，若有则返回true
	 */
	public boolean containsProperty(String name) {
//		List<String> list = getPropertyToList(name) ;
//		return Assert.notNull(list);
		return getProperty(name) != null;
	}

	/**
	 * 从当前属性源中获得此键名对应的属性值，若没有指定的键名则返回null
	 */
	public abstract Object getProperty(String name);

	/**
	 * 获得多值的属性。若只有一个值则容器中只存储此值。若没有指定的键名则返回空集
	 * @param name
	 * @return
	 */
	public abstract List<String> getPropertyToList(String name);
	
	@Override
	public String toString() {
		if (DebugUtils.debug) {
			return getClass().getSimpleName() + "@" + System.identityHashCode(this) +
					" {name='" + this.name + "', properties=" + this.source + "}";
		} else {
			return getClass().getSimpleName() + " {name='" + this.name + "'}";
		}
	}

	/**
	 * 包装那些还未获得属性源对象但必须预先配置到配置环境中的属性源
	 */
	public static class StubPropertySource extends PropertySource<Object> {

		public StubPropertySource(String name) {
			super(name, new Object());
		}

		/**
		 * 总是返回null
		 */
		@Override
		public String getProperty(String name) {
			return null;
		}

		@Override
		public List<String> getPropertyToList(String name) {
			return Collections.emptyList();
		}
	}
	
	/**
	 * 包装正规的PropertySource对象
	 */
	public static class SimplePropertySource extends PropertySource<Map<String, String>> {

		public SimplePropertySource(String name,Map<String, String> source) {
			super(name, source);
		}

		@Override
		public String getProperty(String name) {
			return super.source.get(name);
		}
		
		@Override
		public List<String> getPropertyToList(String name) {
			String property = this.getProperty(name);
			List<String> list = new ArrayList<>();
			if (Assert.hasText(property)) {
				String[] tokenizeToStringArray = StringUtils.tokenizeToStringArray(property, ",", null);
				
				if (tokenizeToStringArray.length == 1) {
					list.add(property);
					return list;
				}
				
				for (String str : tokenizeToStringArray) {
					list.add(str);
				}
			}
			return list;
		}
		
		@Override
		public boolean containsProperty(String name) {
			return this.source.containsKey(name);
		}
		
		@Override
		public String[] getPropertyName() {
			return StringUtils.toStringArray(this.source.keySet());
		}
	}
}

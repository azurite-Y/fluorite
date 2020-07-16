package org.zy.fluorite.beans.support;

import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午12:49:50;
 * @Description
 */
@SuppressWarnings("serial")
public class AutowireCandidateQualifier extends BeanMetadataAttributeAccessor  {
	public static final String VALUE_KEY = "value";
	// @Qualifier注解标注的注解类型
	private final String typeName;
	
	public AutowireCandidateQualifier(Class<?> type) {
		this(type.getName());
	}

	public AutowireCandidateQualifier(String typeName) {
		Assert.notNull(typeName, "类型名称不能为null");
		this.typeName = typeName;
	}

	public AutowireCandidateQualifier(Class<?> type, Object value) {
		this(type.getName(), value);
	}

	public AutowireCandidateQualifier(String typeName, Object value) {
		Assert.notNull(typeName, "类型名称不能为null");
		this.typeName = typeName;
		setAttribute(VALUE_KEY, value);
	}

	public String getTypeName() {
		return this.typeName;
	}
}

package org.zy.fluorite.core.subject;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.zy.fluorite.core.interfaces.AttributeAccessor;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午12:54:27;
 * @Description
 */
@SuppressWarnings("serial")
public abstract class AttributeAccessorSupport implements Serializable,AttributeAccessor {
	
	private final Map<String, Object> attributes = new LinkedHashMap<>();
	
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttribute(String name, Object value) {
		Assert.notNull(name, "name不能为null");
		if (value != null) {
			this.attributes.put(name, value);
		}
		else {
			removeAttribute(name);
		}
	}

	@Override
	public Object getAttribute(String name) {
		Assert.notNull(name, "name不能为null");
		return this.attributes.get(name);
	}

	@Override
	public Object removeAttribute(String name) {
		Assert.notNull(name, "name不能为null");
		return this.attributes.remove(name);
	}

	@Override
	public boolean hasAttribute(String name) {
		Assert.notNull(name, "name不能为null");
		return this.attributes.containsKey(name);
	}

	@Override
	public String[] attributeNames() {
		return StringUtils.toStringArray(this.attributes.keySet());
	}
	
	protected void copyAttributesFrom(AttributeAccessor source) {
		Assert.notNull(source, "源对象不能为null");
		String[] attributeNames = source.attributeNames();
		for (String attributeName : attributeNames) {
			setAttribute(attributeName, source.getAttribute(attributeName));
		}
	}
}

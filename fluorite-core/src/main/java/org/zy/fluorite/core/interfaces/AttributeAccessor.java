package org.zy.fluorite.core.interfaces;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午12:54:06;
 * @Description 属性访问方法定义接口
 */
public interface AttributeAccessor {
	void setAttribute(String name, Object value);

	Object getAttribute(String name);

	Object removeAttribute(String name);

	boolean hasAttribute(String name);

	String[] attributeNames();
}

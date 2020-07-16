package org.zy.fluorite.core.subject;

import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午9:26:03;
 * @Description
 */
@SuppressWarnings("serial")
public class ManagedArray extends ManagedList<Object> {
	volatile Class<?> resolvedElementType;


	public ManagedArray(String elementTypeName, int size) {
		super(size);
		Assert.notNull(elementTypeName, "元素类型名称不能为null");
		setElementTypeName(elementTypeName);
	}

}

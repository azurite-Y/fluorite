package org.zy.fluorite.beans.factory.support;

import org.zy.fluorite.beans.factory.interfaces.NamedBean;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午3:31:48;
 * @Description
 */
public class NamedBeanHolder<T> implements NamedBean {
	private final String beanName;

	private final T beanInstance;
	
	public NamedBeanHolder(String beanName, T beanInstance) {
		super();
		this.beanName = beanName;
		this.beanInstance = beanInstance;
	}

	@Override
	public String getBeanName() {
		return this.beanName;
	}
	
	public T getBeanInstance() {
		return this.beanInstance;
	}
}

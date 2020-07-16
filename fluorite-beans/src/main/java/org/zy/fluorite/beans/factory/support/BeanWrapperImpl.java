package org.zy.fluorite.beans.factory.support;

import org.zy.fluorite.beans.factory.interfaces.BeanWrapper;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月11日 下午11:22:12;
 * @Description
 */
public class BeanWrapperImpl implements BeanWrapper {
	private Object wrappedInstance;
	
	
	public BeanWrapperImpl() {
		super();
	}
	public BeanWrapperImpl(Object wrappedInstance) {
		super();
		this.wrappedInstance = wrappedInstance;
	}

	@Override
	public Object getWrappedInstance() {
		return this.wrappedInstance;
	}

	@Override
	public Class<?> getWrappedClass() {
		return this.wrappedInstance.getClass();
	}

	@Override
	public void setBeanInstance(Object instantiate) {
		this.wrappedInstance = instantiate;
	}


}

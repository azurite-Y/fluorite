package org.zy.fluorite.beans.factory.interfaces;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月10日 下午3:33:33;
 * @Description 
 */
public interface BeanWrapper {
	/**
	 * 返回此对象包装的bean实例
	 */
	Object getWrappedInstance();

	/**
	 * 返回包装bean实例的类型
	 */
	Class<?> getWrappedClass();

	void setBeanInstance(Object instantiate);
	
}

package org.zy.fluorite.beans.factory.interfaces;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午3:28:55;
 * @Description BeanNameAware的对应物。返回对象的bean名称
 */
public interface NamedBean {
	/**
	 * 获得bean的名称
	 * @return
	 */
	String getBeanName();
}

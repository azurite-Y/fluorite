package org.zy.fluorite.beans.factory.interfaces;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 上午10:09:17;
 * @Description 描述bean工厂层次方法的接口
 */
public interface HierarchicalBeanFactory extends BeanFactory {
	/**
	 * 获得父bean工厂
	 * @return
	 */
	BeanFactory getParentBeanFactory();

	/**
	 * 返回本地bean工厂是否包含给定名称的bean，忽略来自祖先bean工厂的给定名称的beano
	 */
	boolean containsLocalBean(String name);
}

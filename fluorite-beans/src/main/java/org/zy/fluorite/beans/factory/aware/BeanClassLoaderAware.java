package org.zy.fluorite.beans.factory.aware;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月11日 下午1:30:53;
 * @Description 感知加载BeanClass对象的ClassLoader实现
 */
public interface BeanClassLoaderAware {

	void setBeanClassLoader(ClassLoader bcl);

}

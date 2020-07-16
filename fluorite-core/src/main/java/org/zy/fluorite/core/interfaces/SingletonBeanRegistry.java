package org.zy.fluorite.core.interfaces;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 上午10:03:41;
 * @Description 单例容器操作方法接口
 */
public interface SingletonBeanRegistry {
	/**
	 * 将指定bean对象注册到单例容器中
	 */
	void registerSingleton(String beanName, Object singletonObject);

	/**
	 * 从单例对象容器中获得beanName对应的单例对象
	 */
	Object getSingleton(String beanName);

	/**
	 * 判断beanName是否存在于单例bean名称容器中，存在则返回true
	 */
	boolean containsSingleton(String beanName);

	/**
	 * 获得单例bean名称容器中所有的bean名称
	 */
	String[] getSingletonNames();

	/**
	 * 获得已注册为单例bean的个数
	 */
	int getSingletonCount();

	/**
	 * 返回此注册表（用于外部协作者）使用的单例互斥体。
	 */
	Object getSingletonMutex();
}

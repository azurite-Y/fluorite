package org.zy.fluorite.core.interfaces.instantiation;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午4:56:57;
 * @Description 工厂bean接口，使用统一的方法实例化某一类对象
 */
public interface FactoryBean<T> {
	/**
	 * 返回已创建好的实例
	 * @return
	 * @throws Exception
	 */
	T getObject() throws Exception;
	
	/**
	 * 返回本工厂bean负责创建的实例类型
	 * @return
	 */
	Class<?> getObjectType();
	
	/**
	 * 判断本工厂bean创建的实例是否是单例的，默认为true则创建对象是单例的
	 * @return
	 */
	default boolean isSingleton() {
		return true;
	}
}

package org.zy.fluorite.core.interfaces.instantiation;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午5:00:48;
 * @Description 智能化的工厂bean，控制工厂bean创建实例的时机
 */
public interface SmartFactoryBean<T> extends FactoryBean<T> {
	/**
	 * 标识工厂Bean创建的对象是否是原型对象，与isSingleton()方法返回值相反。
	 * 当实现此接口时会调用此方法判断是否创建原型对象而不再调用isSingleton()方法
	 * @return
	 */
	default boolean isPrototype() {
		return false;
	}
	/**
	 * 判断工厂bean是否立即创建实例，默认为false则延迟创建
	 * @return
	 */
	default boolean isEagerInit() {
		return false;
	}
}

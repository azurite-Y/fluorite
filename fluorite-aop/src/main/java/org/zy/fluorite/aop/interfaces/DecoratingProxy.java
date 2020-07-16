package org.zy.fluorite.aop.interfaces;

/**
 * @DateTime 2020年7月8日 下午4:47:50;
 * @author zy(azurite-Y);
 * @Description 接口将通过装饰代理来实现
 */
public interface DecoratingProxy {
	/**
	 * 返回此代理的终极装饰类
	 * @return 装饰类，从不为空
	 */
	Class<?> getDecoratedClass();
}

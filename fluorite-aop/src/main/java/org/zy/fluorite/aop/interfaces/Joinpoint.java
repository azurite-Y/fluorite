package org.zy.fluorite.aop.interfaces;

import java.lang.reflect.AccessibleObject;

/**
 * @DateTime 2020年7月5日 上午8:43:40;
 * @author zy(azurite-Y);
 * @Description 此接口表示一个通用的运行时连接点
 * <p>在拦截框架的上下文中，运行时连接点是对可访问对象（方法、构造函数、字段）的访问
 * 的具体化，即连接点的静态部分。它被传递给安装在静态连接点上的拦截器。<br/>
 * 给定连接点的静态部分可以使用getStaticPart（）方法进行检索。
 * </p>
 */
public interface Joinpoint {
	/**
	 * 进入链中的下一个拦截器。此方法的实现和语义依赖于实际的连接点类型
	 * @throws 如果连接点引发异常
	 */
	Object proceed() throws Throwable;

	/** 返回保存当前连接点的静态部分的对象。例如，调用的目标对象。*/
	Object getThis();

	/** 返回此joinpoint的静态部分。静态部分是一个可访问的对象，在这个对象上安装了一系列拦截器 */
	AccessibleObject getStaticPart();

}

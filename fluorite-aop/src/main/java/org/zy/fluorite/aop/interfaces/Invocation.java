package org.zy.fluorite.aop.interfaces;

/**
 * @DateTime 2020年7月5日 上午8:42:05;
 * @author zy(azurite-Y);
 * @Description 此接口表示程序中的调用。调用是一个连接点，可以被侦听器截获
 */
public interface Invocation extends Joinpoint {
	/**
	 * 将参数作为数组获取对象。它可以更改此数组中的元素值来更改参数
	 * @return 调用的参数
	 */
	Object[] getArguments();
}

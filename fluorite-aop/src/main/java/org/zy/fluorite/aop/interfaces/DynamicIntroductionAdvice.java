package org.zy.fluorite.aop.interfaces;

/**
 * @DateTime 2020年7月4日 下午4:02:18;
 * @author zy(azurite-Y);
 * @Description AOP通知的子接口，可动态的引入其他接口方法而不需代理类继承
 */
public interface DynamicIntroductionAdvice extends Advice{
	
	/** 检查指定的Class对象是否实现了给定的接口 */
	boolean implementsInterface(Class<?> intf);
}

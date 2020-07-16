package org.zy.fluorite.aop.aspectj.interfaces;

import java.lang.reflect.Method;

/**
 * @DateTime 2020年7月12日 下午3:53:29;
 * @author zy(azurite-Y);
 * @Description 表示目标类连接点对象。任何一个增强方法都可以通过将第一个入参声明为JoinPoint访问到连接点上下文的信息
 */
public interface JoinPoint {
	
	/** 获取代理对象本身 */
	 Object getThis();
	 
	 /** 获取连接点所在的目标对象 */
	 Object getTarget();

	 /** 获取连接点方法运行时的入参列表 */
	 Object[] getArgs();

	 /** 连接点方法对象 */
	 Method getJoinPointMethod();
}

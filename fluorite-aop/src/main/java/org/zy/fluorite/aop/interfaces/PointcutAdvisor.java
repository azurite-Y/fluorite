package org.zy.fluorite.aop.interfaces;

/**
 * @DateTime 2020年7月6日 下午4:48:43;
 * @author zy(azurite-Y);
 * @Description
 */
public interface PointcutAdvisor extends Advisor {
	/** 获得此Avicsor的切点 */
	Pointcut getPointcut();
}

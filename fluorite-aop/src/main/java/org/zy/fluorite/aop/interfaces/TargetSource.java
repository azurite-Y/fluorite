package org.zy.fluorite.aop.interfaces;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 上午9:55:07;
 * @Description
 */
public interface TargetSource extends TargetClassAware {

	/**
	 * 返回此TargetSource返回的目标类型
	 */
	@Override
	Class<?> getTargetClass();
	
	/**
	 * @return
	 */
	boolean isStatic();

	/**
	 * 返回目标实例。在操作框架调用AOP方法调用的“目标”之前立即调用
	 */
	Object getTarget() ;

	/**
	 * 释放从getTarget（）方法获得的给定目标对象（如果有）
	 */
	void releaseTarget(Object target) throws Exception;
}

package org.zy.fluorite.aop.interfaces;

/**
 * @DateTime 2020年7月4日 下午1:07:55;
 * @author zy(azurite-Y);
 * @Description
 */
public interface Advisor {
	
	/** 如果尚未配置正确的Advice，则从getAdvice（）返回空通知的公共占位符 */
	Advice EMPTY_ADVICE = new Advice() {};


	/** 获得此切面的advice。可以是一个前置通知、后置通知或异常通知 */
	Advice getAdvice();

}

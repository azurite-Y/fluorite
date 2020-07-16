package org.zy.fluorite.core.utils;

import org.zy.fluorite.core.interfaces.AnalysisReporter;

/**
 * @DateTime 2020年6月27日 下午3:39:03;
 * @author zy(azurite-Y);
 * @Description 简易的异常处理工具类
 */
public class FailureReporterUtils {
	
	/**
	 * 报告给定类型的异常
	 * @param exceptionClz - 异常类型
	 * @param exceptionMessage - 异常信息
	 * @param reporter - 具体的异常报告动作，在打印错误流之前执行。如异常收集等
	 * @param logger - 日志打印对象
	 */
	public static void report(Class<? extends RuntimeException> exceptionClz,String exceptionMessage,AnalysisReporter<RuntimeException> reporter) {
		RuntimeException instantiateClass = ReflectionUtils.instantiateClass(exceptionClz,new Object[] {exceptionMessage});
		if (reporter != null) reporter.report(instantiateClass);
		instantiateClass.printStackTrace();
	}
	
	/**
	 * 直接触发指定异常而不进行其他操作
	 * @param exceptionClz
	 * @param exceptionMessage
	 */
	public static void report(Class<? extends RuntimeException> exceptionClz,String exceptionMessage) {
		RuntimeException instantiateClass = ReflectionUtils.instantiateClass(exceptionClz,new Object[] {exceptionMessage});
		instantiateClass.printStackTrace();
	}
}

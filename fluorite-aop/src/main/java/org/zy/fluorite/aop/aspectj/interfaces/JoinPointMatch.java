package org.zy.fluorite.aop.aspectj.interfaces;

import org.zy.fluorite.core.utils.TypeConvertUtils;

/**
 * @DateTime 2020年7月11日 下午11:21:24;
 * @author zy(azurite-Y);
 * @Description 连接点方法的参数绑定器
 */
public interface JoinPointMatch {
	
	/**
	 * 根据参数类型和参数名绑定参数，此间发生的类型转换由 {@linkplain TypeConvertUtils } 类处理
	 * @param targetClass - 期望的参数类型
	 * @param parameterName - 参数名
	 * @return 若可绑定则返回不为null的参数值，反之则返回null
	 */
	Object parameterBinding(Class<?> targetClass , String parameterName);
	
}

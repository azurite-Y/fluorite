package org.zy.fluorite.core.interfaces;

import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午2:15:16;
 * @Description 策略接口，用于通过计算itas表达式（如果适用）来解析值
 */
public interface BeanExpressionResolver {
	String evaluate(String value, Environment environment) throws BeansException;
}

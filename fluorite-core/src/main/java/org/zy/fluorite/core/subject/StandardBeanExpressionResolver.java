package org.zy.fluorite.core.subject;

import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.exception.ExpressionFormatException;
import org.zy.fluorite.core.interfaces.BeanExpressionResolver;

/**
 * @DateTime 2020年6月17日 下午10:49:26;
 * @author zy(azurite-Y);
 * @Description 未完成，现在只支持配置属性注入
 */
public class StandardBeanExpressionResolver implements BeanExpressionResolver {

	/** 默认的表达式前缀: "#{" */
	public static final String DEFAULT_EXPRESSION_PREFIX = "#{";

	/** 默认的表达式后缀: "}" */
	public static final String DEFAULT_EXPRESSION_SUFFIX = "}";

	private String expressionPrefix = DEFAULT_EXPRESSION_PREFIX;

	private String expressionSuffix = DEFAULT_EXPRESSION_SUFFIX;
	
	public StandardBeanExpressionResolver() {
		super();
	}
	public StandardBeanExpressionResolver(String expressionPrefix, String expressionSuffix) {
		super();
		this.expressionPrefix = expressionPrefix;
		this.expressionSuffix = expressionSuffix;
	}

	@Override
	public String evaluate(String value, Environment environment) throws BeansException {
		int length = this.expressionPrefix.length();
		int length2 = this.expressionSuffix.length();
		boolean prefix = value.indexOf(this.expressionPrefix) != -1;
		boolean suffix = value.indexOf(this.expressionSuffix) != -1;
		String result = null;
		if (prefix) {
			if (suffix) {
				value = new String(value.toCharArray(), length, value.length() - length2-2);
				result = getResultToContext(value,environment);
			} else {
				throw new ExpressionFormatException("无法解析的表达式，by：" + value);
			}
		}
		return result;
	}

	/**
	 * 首先检查属性源中是否有指定key。无则返回null
	 * @param value
	 * @param evalContext
	 * @return
	 */
	protected String getResultToContext(String value, Environment environment) {
		return environment.getProperty(value);
	}
	
	public String getExpressionPrefix() {
		return expressionPrefix;
	}
	public void setExpressionPrefix(String expressionPrefix) {
		this.expressionPrefix = expressionPrefix;
	}
	public String getExpressionSuffix() {
		return expressionSuffix;
	}
	public void setExpressionSuffix(String expressionSuffix) {
		this.expressionSuffix = expressionSuffix;
	}
}

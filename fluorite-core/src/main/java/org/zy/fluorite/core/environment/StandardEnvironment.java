package org.zy.fluorite.core.environment;

import org.zy.fluorite.core.environment.interfaces.PropertySource.SimplePropertySource;
import org.zy.fluorite.core.interfaces.BeanExpressionResolver;
import org.zy.fluorite.core.subject.StandardBeanExpressionResolver;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年6月17日 上午9:39:05;
 * @author zy(azurite-Y);
 * @Description 适用于“标准”（即非web）应用程序的环境实现，装配系统属性和系统环境变量
 */
public class StandardEnvironment extends AbstractEnvironment {
	protected BeanExpressionResolver expressionResolver;
	
	public StandardEnvironment() {
		this.expressionResolver = new StandardBeanExpressionResolver();
	}
	public StandardEnvironment(BeanExpressionResolver standardBeanExpressionResolver) {
		this.expressionResolver = new StandardBeanExpressionResolver();
	}
	
	@Override
	public void customizePropertySources() {
		super.customizePropertySources();
		super.propertySources.addLast(new SimplePropertySource(SYSTEM_PROPERTIES, getSystemProperties()));
		super.propertySources.addLast(new SimplePropertySource(SYSTEM_ENVIRONMENT, getSystemEnvironment()));
	}

	@Override
	public String resolvePlaceholders(String text) {
		return expressionResolver.evaluate(text, this);
	}

	@Override
	public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
		String evaluate = expressionResolver.evaluate(text, this);
		Assert.hasText(evaluate, "无法解析的占位符文本，by："+text);
		return evaluate;
	}
}

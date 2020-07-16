package org.zy.fluorite.beans.factory.support;

import org.zy.fluorite.beans.factory.interfaces.ConfigurableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.Scope;
import org.zy.fluorite.core.environment.AbstractEnvironment;
import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午4:44:47;
 * @Description 用于表达式解析的上下文对象
 */
public class BeanExpressionContext {
	private final AbstractEnvironment environment;
	private final ConfigurableBeanFactory beanFactory;
	private final Scope scope;

	
	public BeanExpressionContext(AbstractEnvironment environment, ConfigurableBeanFactory beanFactory, Scope scope) {
		super();
		Assert.notNull(beanFactory, "'BeanFactory'不能为null");
		Assert.notNull(environment, "'Environment'不能为null");
		this.environment = environment;
		this.beanFactory = beanFactory;
		this.scope = scope;
	}

	public final ConfigurableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}
	public AbstractEnvironment getEnvironment() {
		return environment;
	}
	public final Scope getScope() {
		return this.scope;
	}
	public boolean containsObject(String key) {
		return (this.beanFactory.containsBean(key)
				|| (this.scope != null && this.scope.resolveContextualObject(key) != null));
	}
	public Object getObject(String key) {
		if (this.beanFactory.containsBean(key)) {
			return this.beanFactory.getBean(key);
		} else if (this.scope != null) {
			return this.scope.resolveContextualObject(key);
		} else {
			return null;
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanExpressionContext)) {
			return false;
		}
		BeanExpressionContext otherContext = (BeanExpressionContext) other;
		return (this.beanFactory == otherContext.beanFactory && this.scope == otherContext.scope);
	}

	@Override
	public int hashCode() {
		return this.beanFactory.hashCode();
	}

}

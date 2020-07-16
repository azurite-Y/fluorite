package org.zy.fluorite.beans.factory.support;

import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.core.environment.AbstractEnvironment;
import org.zy.fluorite.core.interfaces.BeanExpressionResolver;
import org.zy.fluorite.core.interfaces.StringValueResolver;

/**
 * @DateTime 2020年6月17日 下午10:44:17;
 * @author zy(azurite-Y);
 * @Description
 */
public class EmbeddedValueResolver implements StringValueResolver {

	private final BeanExpressionContext exprContext;
	private final BeanExpressionResolver exprResolver;
	
	
	
	public EmbeddedValueResolver(AbstractEnvironment abstractEnvironment,ConfigurableListableBeanFactory beanFactory) {
		this.exprContext = new BeanExpressionContext(abstractEnvironment,beanFactory, null);
		this.exprResolver = beanFactory.getBeanExpressionResolver();
	}

	@Override
	public String resolveStringValue(String strVal) {
		// 调用BeanFactory的字符串解析器集合解析字符串 
		if (this.exprResolver != null && strVal != null) {
			String evaluated = this.exprResolver.evaluate(strVal, this.exprContext.getEnvironment());
			return evaluated != null ? evaluated : null;
		}
		return null;
	}

}

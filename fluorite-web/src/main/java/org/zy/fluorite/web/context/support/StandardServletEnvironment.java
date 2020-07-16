package org.zy.fluorite.web.context.support;

import org.zy.fluorite.core.environment.StandardEnvironment;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.environment.interfaces.PropertySource.StubPropertySource;
import org.zy.fluorite.core.interfaces.BeanExpressionResolver;

/**
 * @DateTime 2020年6月17日 上午10:02:12;
 * @author zy(azurite-Y);
 * @Description 基于Servlet的web应用程序要使用的环境实现
 */
public class StandardServletEnvironment extends StandardEnvironment implements ConfigurableEnvironment {
	public StandardServletEnvironment() {
		super();
	}
	public StandardServletEnvironment(BeanExpressionResolver standardBeanExpressionResolver) {
		super(standardBeanExpressionResolver);
	}

	@Override
	public void customizePropertySources() {
		super.customizePropertySources();
		propertySources.addLast(new StubPropertySource(SERVLET_CONTEXT_INIT_PROPERTY));
		propertySources.addLast(new StubPropertySource(SERVLET_CONFIG_INIT_PROPERTY));
	}
	
	
}

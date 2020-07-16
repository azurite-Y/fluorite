package org.zy.fluorite.context.support.initializer;

import java.util.concurrent.atomic.AtomicLong;

import org.zy.fluorite.context.interfaces.ApplicationContext;
import org.zy.fluorite.context.interfaces.ApplicationContextInitializer;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;
import org.zy.fluorite.core.environment.Property;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年6月25日 下午5:44:45;
 * @author zy(azurite-Y);
 * @Description 从配置文件中获得‘fluorite.application.name’的值生成ContextId对象，并保存到ApplicationContext和注册单例到BeanFactory中
 */
public class ContextIdApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>,Ordered {

	private int order = Ordered.LOWEST_PRECEDENCE - 10;
	
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}
	
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		ContextId contextId = getContextId(applicationContext);
		applicationContext.setId(contextId.getId());
		applicationContext.getBeanFactory().registerSingleton(ContextId.class.getName(), contextId);
	}

	private ContextId getContextId(ConfigurableApplicationContext applicationContext) {
		ApplicationContext parent = applicationContext.getParent();
		if (parent != null && parent.containsBean(ContextId.class.getName())) {
			return parent.getBean(ContextId.class).createChildId();
		}
		return new ContextId(getApplicationId(applicationContext.getEnvironment()));
	}
	
	private String getApplicationId(ConfigurableEnvironment environment) {
		String name = environment.getProperty(Property.APPLICATION_NAME);
		return Assert.hasText(name) ? name : "application";
	}
	
	static class ContextId {

		private final AtomicLong children = new AtomicLong(0);

		private final String id;

		ContextId(String id) {
			this.id = id;
		}

		ContextId createChildId() {
			return new ContextId(this.id + "-" + this.children.incrementAndGet());
		}

		String getId() {
			return this.id;
		}

	}
}

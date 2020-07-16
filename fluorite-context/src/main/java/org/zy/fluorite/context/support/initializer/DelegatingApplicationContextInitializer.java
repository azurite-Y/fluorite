package org.zy.fluorite.context.support.initializer;

import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.context.interfaces.ApplicationContextInitializer;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;
import org.zy.fluorite.core.environment.Property;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ReflectionUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年6月25日 下午5:22:22;
 * @author zy(azurite-Y);
 * @Description 从配置文件中获得context.initializer.classes的值并将其实例化为ApplicationContextInitializer实现，然后调用initialize(...)方法
 */
public class DelegatingApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		ConfigurableEnvironment environment = applicationContext.getEnvironment();
		List<Class<? extends ApplicationContextInitializer<ConfigurableApplicationContext>>> initializerClasses = getInitializerClasses(environment);
		if (!initializerClasses.isEmpty()) {
			applyInitializerClasses(applicationContext, initializerClasses);
		}
	}

	private void applyInitializerClasses(ConfigurableApplicationContext applicationContext,	List<Class<? extends ApplicationContextInitializer<ConfigurableApplicationContext>>> initializerClasses) {
		initializerClasses.forEach( initializerClz -> {
			ApplicationContextInitializer<ConfigurableApplicationContext> instantiateClass = ReflectionUtils.instantiateClass(initializerClz);
			instantiateClass.initialize(applicationContext);
		});
		
	}

	private List<Class<? extends ApplicationContextInitializer<ConfigurableApplicationContext>>> getInitializerClasses(ConfigurableEnvironment environment) {
		String classNames = environment.getProperty(Property.INITIALIZER_CLASSES);
		List<Class<? extends ApplicationContextInitializer<ConfigurableApplicationContext>>> classes = new ArrayList<>();
		if (Assert.hasText(classNames)) {
			StringUtils.tokenizeToStringArray(classNames, ",", (str) -> {
				classes.add(getInitializerClass(str));
			});
		}
		return classes;
	}

	@SuppressWarnings("unchecked")
	private Class<? extends ApplicationContextInitializer<ConfigurableApplicationContext>> getInitializerClass(String className) {
		Class<?> forName = ReflectionUtils.forName(className);
		boolean assignableFrom = ApplicationContextInitializer.class.isAssignableFrom(forName);
		Assert.isTrue(assignableFrom, "加载ApplicationContextInitializer的实现类失败，'"+className+"'不是ApplicationContextInitializer接口实现");
		return (Class<? extends ApplicationContextInitializer<ConfigurableApplicationContext>>)forName;
	}

}

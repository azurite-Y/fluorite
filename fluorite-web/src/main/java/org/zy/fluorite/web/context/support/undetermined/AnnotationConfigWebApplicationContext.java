package org.zy.fluorite.web.context.support.undetermined;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.zy.fluorite.beans.factory.interfaces.AutowireCapableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.BeanNameGenerator;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.context.interfaces.AnnotationConfigRegistry;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;
import org.zy.fluorite.context.support.DefaultListableBeanFactory;
import org.zy.fluorite.context.support.undetermined.AbstractRefreshableConfigApplicationContext;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.web.context.support.StandardServletEnvironment;

/**
 * @DateTime 2020年6月17日 下午6:04:02;
 * @author zy(azurite-Y);
 * @Description
 */
@Deprecated
@SuppressWarnings("unused")
public class AnnotationConfigWebApplicationContext extends AbstractRefreshableConfigApplicationContext
		implements ConfigurableApplicationContext,AnnotationConfigRegistry {
	
	private BeanNameGenerator beanNameGenerator;

	private final Set<Class<?>> componentClasses = new LinkedHashSet<>();

	private final Set<String> basePackages = new LinkedHashSet<>();
	
	public AnnotationConfigWebApplicationContext() {
		setDisplayName("Root-WebApplicationContext");
	}
	
	public BeanNameGenerator getBeanNameGenerator() {
		return beanNameGenerator;
	}
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
	}

	@Override
	public ConfigurableEnvironment createEnvironment() {
		return new StandardServletEnvironment();
	}

	
	
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void register(Class<?>... componentClasses) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void scan(String... basePackages) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public boolean isTypeMatch(String name, ResolvableType typeToMatch, boolean allowEagerInit) {
		// TODO 自动生成的方法存根
		return false;
	}

	@Override
	public boolean isTypeMatch(String name, Class<?> typeToMatch) {
		// TODO 自动生成的方法存根
		return false;
	}

	@Override
	public boolean isTypeMatch(String name, ResolvableType typeToMatch) {
		// TODO 自动生成的方法存根
		return false;
	}

	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		// TODO 自动生成的方法存根
		return null;
	}

	@Override
	public ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
		return null;
	}
}

package org.zy.fluorite.autoconfigure.web.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.context.interfaces.AnnotationConfigRegistry;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年6月19日 上午10:01:22;
 * @author zy(azurite-Y);
 * @Description
 */
public class AnnotationConfigServletWebServerApplicationContext extends ServletWebServerApplicationContext
		implements AnnotationConfigRegistry {

	private final List<Class<?>> annotatedClasses = new ArrayList<>();

	@SuppressWarnings("unused")
	private String[] basePackages;
	
	
	public AnnotationConfigServletWebServerApplicationContext() {
		super();
	}
	public AnnotationConfigServletWebServerApplicationContext(String... basePackages) {
		this();
		scan(basePackages);
		refresh();
	}
	
	@Override
	protected void prepareRefresh() {
		super.prepareRefresh();
	}

	@Override
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		super.postProcessBeanFactory(beanFactory);
	}

	@Override
	public void register(Class<?>... componentClasses) {
		Assert.notNull(annotatedClasses, "必须至少指定一个注解标注类");
		this.annotatedClasses.addAll(Arrays.asList(componentClasses));
	}

	@Override
	public void scan(String... basePackages) {
		Assert.notNull(basePackages, "必须至少指定一个包路径");
		this.basePackages = basePackages;
	}
}

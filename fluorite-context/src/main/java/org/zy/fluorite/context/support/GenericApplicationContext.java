package org.zy.fluorite.context.support;

import java.util.concurrent.atomic.AtomicBoolean;

import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.exception.BeanDefinitionStoreException;
import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.beans.factory.interfaces.AutowireCapableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.context.interfaces.ApplicationContext;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年6月18日 下午11:10:20;
 * @author zy(azurite-Y);
 * @Description
 */
public abstract class GenericApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {
	private final DefaultListableBeanFactory beanFactory;

	private final AtomicBoolean refreshed = new AtomicBoolean();

	public GenericApplicationContext() {
		super();
		this.beanFactory = new DefaultListableBeanFactory();
	}

	public GenericApplicationContext(DefaultListableBeanFactory beanFactory) {
		Assert.notNull(beanFactory, "BeanFactory不能为null");
		this.beanFactory = beanFactory;
	}

	public GenericApplicationContext(ApplicationContext parent) {
		this();
		setParent(parent);
	}

	public GenericApplicationContext(DefaultListableBeanFactory beanFactory, ApplicationContext parent) {
		this(beanFactory);
		setParent(parent);
	}

	@Override
	public void registerAlias(String beanName, String alias) {
		this.beanFactory.registerAlias(beanName, alias);
	}

	@Override
	public void removeAlias(String alias) {
		this.beanFactory.removeAlias(alias);
	}

	@Override
	public boolean isAlias(String beanName) {
		return this.beanFactory.isAlias(beanName);
	}
	
	@Override
	public <T> T getBean(String name, Class<T> requiredType, Object... args) {
		return null;
	}

	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
		this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
	}

	@Override
	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		this.beanFactory.removeBeanDefinition(beanName);
	}

	@Override
	public RootBeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		return this.beanFactory.getBeanDefinition(beanName);
	}

	@Override
	public boolean isBeanNameInUse(String beanName) {
		return this.beanFactory.isBeanNameInUse(beanName);
	}

	@Override
	protected void refreshBeanFactory() throws BeansException, IllegalStateException {
		// compareAndSet：如果当前值=预期值，原子地将该值设置为给定的更新值且返回true。若不等于预期值则返回false
		Assert.isTrue(this.refreshed.compareAndSet(false, true), "GenericApplicationContext不支持多次刷新尝试：只需调用一次“刷新”");
		this.beanFactory.setSerializationId(getId());
	}

	@Override
	protected void cancelRefresh(BeansException ex) {
		this.beanFactory.setSerializationId(null);
		super.cancelRefresh(ex);
	}
	
	@Override
	protected void closeBeanFactory() {
		this.beanFactory.setSerializationId(null);
	}
	
	@Override
	public final ConfigurableListableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	public final DefaultListableBeanFactory getDefaultListableBeanFactory() {
		return this.beanFactory;
	}
	
	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		assertBeanFactoryActive();
		return this.beanFactory;
	}
	
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
//		this.beanFactory.setEnvironment(environment);
		super.setEnvironment(environment);
	}
}

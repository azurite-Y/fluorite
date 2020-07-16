package org.zy.fluorite.context.support.undetermined;

import java.io.IOException;

import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.context.exception.ApplicationContextException;
import org.zy.fluorite.context.interfaces.ApplicationContext;
import org.zy.fluorite.context.support.AbstractApplicationContext;
import org.zy.fluorite.context.support.DefaultListableBeanFactory;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年6月17日 下午3:44:24;
 * @author zy(azurite-Y);
 * @Description
 */
@Deprecated
public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext {

	private DefaultListableBeanFactory beanFactory;

	/** BeanFactory的同步锁对象 */
	private final Object beanFactoryMonitor = new Object();

	public Object getBeanFactoryMonitor() {
		return beanFactoryMonitor;
	}
	public void setBeanFactory(DefaultListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	@Override
	public ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
		synchronized (this.beanFactoryMonitor) {
			Assert.notNull(beanFactory,"BeanFactory未初始化或已关闭，要通过ApplicationContext访问Bean需调用'refresh'方法");
			return this.beanFactory;
		}
	}

	public AbstractRefreshableApplicationContext() {}
	public AbstractRefreshableApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType, Object... args) {
		return this.beanFactory.getBean(name, requiredType, args);
	}

	@Override
	protected void closeBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory != null) {
				this.beanFactory.setSerializationId(null);
				this.beanFactory = null;
			}
		}
	}

	protected final boolean hasBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			return (this.beanFactory != null);
		}
	}
	
	@Override
	protected void refreshBeanFactory() throws BeansException, IllegalStateException {
		if (hasBeanFactory()) {
			destroyBeans();
			closeBeanFactory();
		}
		try {
			DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
			beanFactory.setSerializationId(getId());
			loadBeanDefinitions(beanFactory);
			synchronized (this.beanFactoryMonitor) {
				this.beanFactory = beanFactory;
			}
		} catch (IOException ex) {
			throw new ApplicationContextException("解析的BeanDefinition时发生I/O错误 " + getDisplayName(),
					ex);
		}
	}

	@Override
	protected void cancelRefresh(BeansException ex) {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory != null) {
				this.beanFactory.setSerializationId(null);
			}
		}
		super.cancelRefresh(ex);
	}
	
	/**
	 * 将bean定义加载到给定的bean工厂中，通常是通过委派给一个或多个bean定义读取器
	 * @param beanFactory
	 * @throws BeansException
	 * @throws IOException
	 */
	protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException;

}

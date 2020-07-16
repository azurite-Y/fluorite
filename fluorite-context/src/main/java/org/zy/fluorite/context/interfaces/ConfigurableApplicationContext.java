package org.zy.fluorite.context.interfaces;

import java.io.Closeable;

import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanFactoryPostProcessor;
import org.zy.fluorite.context.event.interfaces.ApplicationListener;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @DateTime 2020年6月17日 下午1:35:11;
 * @author zy(azurite-Y);
 * @Description 定义上下文配置和生命周期方法的接口
 */
public interface ConfigurableApplicationContext extends ApplicationContext, Lifecycle, Closeable {
	/** 上下文关闭挂钩 */
	String SHUTDOWN_HOOK_THREAD_NAME = "ContextShutdownHook";
	
	void setId(String id);
	
	void setParent(ApplicationContext parent);

	void setEnvironment(ConfigurableEnvironment environment);

	@Override
	ConfigurableEnvironment getEnvironment();

	/**
	 * 注册BeanFactoryPostProcessor到此上下文中
	 */
	void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor);

	/**
	 * 注册事件监听器
	 */
	void addApplicationListener(ApplicationListener<?> listener);

	/**
	 * 刷新上下文，生成注册的单例Bean
	 */
	void refresh() throws BeansException, IllegalStateException;

	/**
	 * 向JVM运行时注册一个关闭锁，在JVM关闭时关闭此上下文，除非此时它已经关闭。
	 * 关闭钩子线程的名称应该是 {@value #SHUTDOWN_HOOK_THREAD_NAME}
	 */
	void registerShutdownHook();

	/**
	 * 关闭此应用程序上下文，释放实现可能保留的所有资源和锁。这包括销毁所有缓存的singleton bean。
	 * <p>注意：不在父上下文上调用close；父上下文有自己的独立生命周期。</p>
	 * 可以多次调用此方法，但不会产生副作用：将忽略对已关闭上下文的后续关闭调用。
	 */
	@Override
	void close();

	/**
	 * 确定此应用程序上下文是否处于活动状态，即它是否已至少刷新一次且尚未关闭
	 */
	boolean isActive();

	ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;

}

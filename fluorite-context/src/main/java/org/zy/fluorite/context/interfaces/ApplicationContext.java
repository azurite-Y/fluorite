package org.zy.fluorite.context.interfaces;

import org.zy.fluorite.beans.factory.interfaces.AutowireCapableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.HierarchicalBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ListableBeanFactory;
import org.zy.fluorite.context.event.interfaces.ApplicationEventPublisher;
import org.zy.fluorite.core.environment.interfaces.EnvironmentCapable;

/**
 * @DateTime 2020年6月17日 下午1:30:05;
 * @author zy(azurite-Y);
 * @Description
 */
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
	MessageSource, ApplicationEventPublisher {

	/**
	 * 返回此应用程序上下文的唯一id
	 * @return 上下文的唯一id，如果没有则为空
	 */
	String getId();

	/**
	 * 已部署应用程序的名称，或默认情况下为空字符串
	 */
	String getApplicationName();

	/**
	 * 获得此上下文的显示名称（从不为空）
	 */
	String getDisplayName();

	/**
	 * 返回首次加载此上下文时的时间戳
	 */
	long getStartupDate();

	/**
	 * 获得父类上下文
	 */
	ApplicationContext getParent();

	/**
	 * 为此上下文公开AutowireCapableBeanFactory功能。
	 * 这通常不被应用程序代码使用，除非是为了初始化位于应用程序上下文之外的bean实例，
	 * 对它们应用springbean生命周期（全部或部分）
	 */
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;
}

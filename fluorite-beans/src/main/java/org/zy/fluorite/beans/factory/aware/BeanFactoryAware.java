package org.zy.fluorite.beans.factory.aware;

import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.Aware;

/**
 * @author: zy（azurite-Y）;
 * @DateTime: 2020年6月4日 下午3:53:35;
 * @Description 使实现类可获得BeanFactory实现的引用
 */
public interface BeanFactoryAware extends Aware {
	
	/**
	 * 运行时回调方法，通过此回调设置BeanFactory实现
	 * @param beanFactory
	 * @throws BeansException
	 */
	void setBeanFactory(BeanFactory beanFactory) throws BeansException;
	
}

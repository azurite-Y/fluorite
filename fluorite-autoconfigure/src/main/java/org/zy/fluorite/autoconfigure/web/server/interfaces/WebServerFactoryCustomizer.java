package org.zy.fluorite.autoconfigure.web.server.interfaces;

import org.zy.fluorite.beans.factory.interfaces.processor.BeanPostProcessor;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description
 * 
 * 定制web服务器工厂的策略界面。这种类型的任何bean都将在服务器自身启动之前从 {@link WebServerFactory web服务器工厂 } 获得回调，
 * 因此您可以设置端口、地址、错误页面等。
 * 
 * 注意：对该接口的调用通常来自 {@link WebServerFactoryCustomizerBeanPostProcessor} ，
 * 它是一个 {@link BeanPostProcessor} （在ApplicationContext生命周期的早期被称为）。
 * 在封闭的BeanFactory中懒洋洋地查找依赖项可能比用 {@code @Autowired} 注入依赖项更安全。
 */
@FunctionalInterface
public interface WebServerFactoryCustomizer<T extends WebServerFactory> {

	/**
	 * 自定义指定的 {@link WebServerFactory}
	 * @param factory - 要自定义的web服务器工厂
	 */
	void customize(T factory);

}

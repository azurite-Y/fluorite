package org.zy.fluorite.boot.interfaces;

import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;

/**
 * @DateTime 2020年6月25日 下午11:03:54;
 * @author zy(azurite-Y);
 * @Description 总揽FluoriteApplication运行期间所触发各种事件，并多播到对应的事件监听器中 
 */
public interface FluoriteApplicationRunListener {
	/**
	 * 在run方法首次启动时立即调用。可用于非常早的初始化
	 */
	default void starting() {}

	/**
	 * 在准备好环境后，但在创建ApplicationContext之前调用
	 */
	default void environmentPrepared(ConfigurableEnvironment environment) {}

	/**
	 * 在创建并准备好ApplicationContext之后，但在加载Bean之前调用
	 */
	default void contextPrepared(ConfigurableApplicationContext context) {}

	/**
	 * 在加载应用程序上下文但在重新刷新之前调用
	 */
	default void contextLoaded(ConfigurableApplicationContext context) {
	}

	/**
	 * 上下文已刷新，应用程序已启动，但尚未调用命令行运行程序和应用程序运行程序。
	 */
	default void started(ConfigurableApplicationContext context) {
	}

	/**
	 * 在run方法完成之前立即调用，此时applicationcontext已刷新并且所有命令行运行程序和应用程序运行程序都已调用。
	 */
	default void running(ConfigurableApplicationContext context) {
	}

	/**
	 * 在运行应用程序时发生故障时调用。
	 */
	default void failed(ConfigurableApplicationContext context, Throwable exception) {
	}
}

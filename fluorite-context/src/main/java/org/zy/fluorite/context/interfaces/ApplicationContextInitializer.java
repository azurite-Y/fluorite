package org.zy.fluorite.context.interfaces;

/**
 * @DateTime 2020年6月19日 下午1:55:26;
 * @author zy(azurite-Y);
 * @Description 应用程序上下文初始化接口，在刷新上下文之前被调用
 */
public interface ApplicationContextInitializer<C extends ConfigurableApplicationContext> {
	/**
	 * 初始化给定的应用程序上下文
	 * 
	 * @param applicationContext
	 */
	void initialize(C applicationContext);
	
}

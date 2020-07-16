package org.zy.fluorite.boot.environment;

import org.zy.fluorite.boot.FluoriteApplication;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;

/**
 * @DateTime 2020年6月26日 下午5:15:20;
 * @author zy(azurite-Y);
 * @Description 允许在刷新应用程序上下文之前自定义应用程序的环境。
 * 实现类在META-INF目录下的Fluorite.facrories中注册
 */
@FunctionalInterface
public interface EnvironmentPostProcessor {
	/**
	 * 后处理给定环境。
	 */
	void postProcessEnvironment(ConfigurableEnvironment environment, FluoriteApplication application);
}

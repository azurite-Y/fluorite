package org.zy.fluorite.core.environment.interfaces;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月16日 下午3:16:58;
 * @Description
 */
public interface Environment extends PropertyResolver {
	static final String ENVIRONMENT_BEAN_NAME = "environment";
	static final String SYSTEM_PROPERTIES = "systemProperties";
	static final String SYSTEM_ENVIRONMENT = "systemEnvironment";
	static final String CONFIG_PROPERTY = "configProperty";
	static final String SERVLET_CONTEXT_INIT_PROPERTY = "servletContextInitParams";
	static final String SERVLET_CONFIG_INIT_PROPERTY = "servletConfigInitParams";
	
	String[] getActiveProfiles();

	String[] getDefaultProfiles();

	/**
	 * 判断指定的前缀代表的配置文件是否处于启用状态
	 */
	boolean acceptsProfiles(String... profile);
}

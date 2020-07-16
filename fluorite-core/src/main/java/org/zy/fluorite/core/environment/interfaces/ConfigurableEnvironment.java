package org.zy.fluorite.core.environment.interfaces;

import java.util.Map;

import org.zy.fluorite.core.environment.MutablePropertySources;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月16日 下午3:40:17;
 * @Description 大多数环境需要实现的接口，对应了环境配置的相关方法
 */
public interface ConfigurableEnvironment extends Environment,PropertyResolver {
	
	/**
	 * 指定此环境的活动配置文件集。配置文件在容器引导期间进行评估，以确定是否应该向容器注册bean定义。
	 */
	void setActiveProfiles(String... profiles);

	/**
	 * 将配置文件添加到当前活动配置文件集
	 */
	void addActiveProfile(String profile);

	/**
	 * 如果没有其他配置文件通过 {@link #setActiveProfiles()} 显式激活，则指定要在默认情况下激活的配置文件集
	 */
	void setDefaultProfiles(String... profiles);

	/**
	 * 获得配置环境中存储所有的属性源MutablePropertySources对象
	 */
	MutablePropertySources getPropertySources();
	
	/** 判断指定名称的属性源是否存在 */
	boolean  containPropertySources(String name);

	/**
	 * 获得系统属性
	 */
	Map<String, String> getSystemProperties();

	/**
	 * 获得系统环境配置
	 */
	Map<String, String> getSystemEnvironment();

	/**
	 * 设置父类的ConfigurableEnvironment持有的PropertySource对象
	 */
	void merge(ConfigurableEnvironment parent);

	/** 配置PropertySource对象，可通过此方法控制数据源的优先级 */
	void customizePropertySources();
}

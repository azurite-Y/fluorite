package org.zy.fluorite.core.environment;

/**
 * @DateTime 2020年6月16日 下午6:25:59;
 * @author zy(azurite-Y);
 * @Description 配置文件定义属性Key
 */
public final class Property {
	/** 应用程序名称-fluorite.application.name */
	public static final String APPLICATION_NAME = "fluorite.application.name";
	
	/** 内镶容器端口号-server.port */
	public static final String SERVER_PORT = "server.port";
	
	/** 激活的配置文件后缀-fluorite.profiles.active */
	public static final String PROFILES_ACTIVVE = "fluorite.profiles.active";
	
	/** 默认的配置文件后缀-fluorite.profiles.default */
	public static final String PROFILES_DEFAULT = "fluorite.profiles.default";
	
	/** 配置不建议扫描的包路径-fluorite.scanner.problematic.packages */
	public static final String PROBLEM_PACKAGES = "fluorite.scanner.problematic.packages";
	
	/** 配置组件索引文件的存储位置（相对与项目根目录的路径），默认存储于项目根目录下。此路径需以“/”结尾 */
	public static final String COMPONENT_INDEX = "fluorite.scanner.component.index";
	
	/** 配置组件索引文件的名称，默认为 'component.index'  */
	public static final String COMPONENT_INDEX_NAME = "fluorite.scanner.component.indexName";
	
	/** 配置要使用的ApplicationContextInitializer接口实现-context.initializer.classes */
	public static final String INITIALIZER_CLASSES = "context.initializer.classes";
	
	/** 配置要使用的ApplicationListener接口实现-context.listener.classes */
	public static final String LISTENER_CLASSES = "context.listener.classes";
	
	/** 设置非存储于在classpath路径下的'banner.txt'文件的路径. 路径格式为java的File对象支持的格式 */
	public static final String BANNER_LOCATION = "fluorite.banner.location";
}

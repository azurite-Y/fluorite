package org.zy.fluorite.context.interfaces;

/**
 * @DateTime 2020年6月17日 下午6:06:38;
 * @author zy(azurite-Y);
 * @Description 注释配置应用程序上下文的公共接口，定义寄存器和扫描方法
 */
public interface AnnotationConfigRegistry {
	/**
	 * 注册一个或多个要处理的组件类
	 */
	void register(Class<?>... componentClasses);

	/**
	 * 在指定的基本包内执行扫描
	 */
	void scan(String... basePackages);
}

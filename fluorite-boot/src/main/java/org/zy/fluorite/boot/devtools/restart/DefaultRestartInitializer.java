package org.zy.fluorite.boot.devtools.restart;

import java.net.URL;

import org.zy.fluorite.boot.interfaces.RestartInitializer;

/**
 * @dateTime 2022年12月28日;
 * @author zy(azurite-Y);
 * @description 默认 {@link RestartInitializer} ，仅在运行标准“main”方法时启用初始重新启动。
 */
public class DefaultRestartInitializer implements RestartInitializer {
	
	@Override
	public URL[] getInitialUrls(Thread thread) {
		if (!isMain(thread)) {
			return null;
		}
		return getUrls(thread);
	}

	/**
	 * 返回线程是否用于主调用。默认情况下，检查线程和上下文类加载器的名称。
	 * 
	 * @param thread - 要检查的线程
	 * @return 如果线程是主线程则为{@code true}
	 */
	protected boolean isMain(Thread thread) {
		return thread.getName().equals("main") && thread.getContextClassLoader().getClass().getName().contains("AppClassLoader");
	}

	/**
	 * @param thread - 源线程
	 * @return 初始化时应使用的URL
	 */
	protected URL[] getUrls(Thread thread) {
		URL file = thread.getContextClassLoader().getResource("");
		URL[] arr = {file};
		return arr;
	}

}

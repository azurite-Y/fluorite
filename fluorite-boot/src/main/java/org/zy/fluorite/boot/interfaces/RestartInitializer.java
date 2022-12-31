package org.zy.fluorite.boot.interfaces;

import java.net.URL;

/**
 * @dateTime 2022年12月28日;
 * @author zy(azurite-Y);
 * @description 用于初始化Restarter的策略接口
 */
@FunctionalInterface
public interface RestartInitializer {

	/**
	 * {@link RestartInitializer} 它不返回任何url.
	 */
	RestartInitializer NONE = (thread) -> null;

	/**
	 * 返回 {@link Restarter} 的初始url集，如果不需要初始重启，则返回null。
	 * 
	 * @param thread - 源线程
	 * @return 初始url集或 {@code null}
	 */
	URL[] getInitialUrls(Thread thread);

}

package org.zy.fluorite.autoconfigure.web.server.interfaces;

import org.zy.fluorite.autoconfigure.web.server.ErrorPage;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description 由注册 {@link ErrorPage ErrorPages} 的类型实现的接口
 */
@FunctionalInterface
public interface ErrorPageRegistrar {

	/**
	 * 根据需要在给定注册表中注册页面
	 * 
	 * @param registry - 错误页注册表
	 */
	void registerErrorPages(ErrorPageRegistry registry);

}

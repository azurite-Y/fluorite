package org.zy.fluorite.autoconfigure.web.server.interfaces;

import org.zy.fluorite.autoconfigure.web.server.ErrorPage;

/**
 * @dateTime 2021年12月23日;
 * @author zy(azurite-Y);
 * @description 错误界面的注册接口
 */
public interface ErrorPageRegistry {
	/**
	 * 添加将在处理异常时使用的错误页
	 * @param errorPages the error pages
	 */
	void addErrorPages(ErrorPage... errorPages);
}

package org.zy.fluorite.autoconfigure.web.server.moonstone;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.autoconfigure.web.servlet.interfaces.ServletContextInitializer;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description {@link ServletContainerInitializer } 用于触发 {@link Servletcontextinitializer }并跟踪启动错误。
 */
class MoonStoneStarter implements ServletContainerInitializer {
	private static final Logger logger = LoggerFactory.getLogger(MoonStoneStarter.class);

	private final ServletContextInitializer[] initializers;

	private volatile Exception startUpException;

	MoonStoneStarter(ServletContextInitializer[] initializers) {
		this.initializers = initializers;
	}

	@Override
	public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {
		try {
			for (ServletContextInitializer initializer : this.initializers) {
				initializer.onStartup(servletContext);
			}
		}
		catch (Exception ex) {
			this.startUpException = ex;
			// 当知道可以在主线程中处理它时，防止 MoonStone 记录日志并重新抛出，但请在这里记录信息。
			if (logger.isErrorEnabled()) {
				logger.error("MoonStone 启动上下文错误, Exception: " + ex.getClass().getName() + ". Message: " + ex.getMessage());
			}
		}
	}

	Exception getStartUpException() {
		return this.startUpException;
	}
}

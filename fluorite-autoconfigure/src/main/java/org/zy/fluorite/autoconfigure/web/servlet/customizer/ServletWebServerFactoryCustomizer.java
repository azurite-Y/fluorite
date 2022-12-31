package org.zy.fluorite.autoconfigure.web.servlet.customizer;

import org.zy.fluorite.autoconfigure.web.ServerProperties;
import org.zy.fluorite.autoconfigure.web.server.interfaces.ConfigurableServletWebServerFactory;
import org.zy.fluorite.autoconfigure.web.server.interfaces.WebServerFactoryCustomizer;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.utils.PropertyMapper;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description  {@link WebServerFactoryCustomizer} 将 {@link ServerProperties} 应用到servlet web服务器。
 */
public class ServletWebServerFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>, Ordered {

	private final ServerProperties serverProperties;

	public ServletWebServerFactoryCustomizer(ServerProperties serverProperties) {
		this.serverProperties = serverProperties;
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		map.from(this.serverProperties::getPort).to(factory::setPort);
		map.from(this.serverProperties::getAddress).to(factory::setAddress);
		// 主上下文配置路径默认为""
//		map.from(this.serverProperties.getServlet()::getContextPath).to(factory::setContextPath);
		map.from(this.serverProperties.getServlet()::getApplicationDisplayName).to(factory::setDisplayName);
		map.from(this.serverProperties.getServlet()::isRegisterDefaultServlet).to(factory::setRegisterDefaultServlet);
		map.from(this.serverProperties.getServlet()::getSession).to(factory::setSession);
//		map.from(this.serverProperties::getSsl).to(factory::setSsl);
		map.from(this.serverProperties::getCompression).to(factory::setCompression);
//		map.from(this.serverProperties::getHttp2).to(factory::setHttp2);
		map.from(this.serverProperties::getServerHeader).to(factory::setServerHeader);
		map.from(this.serverProperties.getServlet()::getContextParameters).to(factory::setInitParameters);
//		map.from(this.serverProperties.getShutdown()).to(factory::setShutdown);
	}

}

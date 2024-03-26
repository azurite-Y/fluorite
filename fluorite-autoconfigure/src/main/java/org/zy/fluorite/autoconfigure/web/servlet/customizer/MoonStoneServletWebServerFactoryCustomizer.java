package org.zy.fluorite.autoconfigure.web.servlet.customizer;

import org.zy.fluorite.autoconfigure.web.ServerProperties;
import org.zy.fluorite.autoconfigure.web.server.interfaces.WebServerFactoryCustomizer;
import org.zy.fluorite.autoconfigure.web.server.moonstone.MoonstoneServletWebServerFactory;
import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description  {@link WebServerFactoryCustomizer} 将 {@link ServerProperties} 应用于 MoonStone Web服务器
 */
public class MoonstoneServletWebServerFactoryCustomizer implements WebServerFactoryCustomizer<MoonstoneServletWebServerFactory>, Ordered {
	private final ServerProperties serverProperties;

	public MoonstoneServletWebServerFactoryCustomizer(ServerProperties serverProperties) {
		this.serverProperties = serverProperties;
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public void customize(MoonstoneServletWebServerFactory factory) {
		ServerProperties.Moonstone moonstoneProperties = this.serverProperties.getMoonstone();
		factory.addContextCustomizers((context) -> context.setMapperContextRootRedirectEnabled(moonstoneProperties.getRedirectContextRoot()));
	}
}

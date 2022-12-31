package org.zy.fluorite.autoconfigure.web.servlet.customizer;

import org.zy.fluorite.autoconfigure.web.ServerProperties;
import org.zy.fluorite.autoconfigure.web.server.interfaces.WebServerFactoryCustomizer;
import org.zy.fluorite.autoconfigure.web.server.moonstone.MoonStoneServletWebServerFactory;
import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description  {@link WebServerFactoryCustomizer} 将 {@link ServerProperties} 应用于 MoonStone Web服务器
 */
public class MoonStoneServletWebServerFactoryCustomizer implements WebServerFactoryCustomizer<MoonStoneServletWebServerFactory>, Ordered {
	private final ServerProperties serverProperties;

	public MoonStoneServletWebServerFactoryCustomizer(ServerProperties serverProperties) {
		this.serverProperties = serverProperties;
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public void customize(MoonStoneServletWebServerFactory factory) {
		ServerProperties.MoonStone moonStoneProperties = this.serverProperties.getMoonStone();
		factory.addContextCustomizers((context) -> context.setMapperContextRootRedirectEnabled(moonStoneProperties.getRedirectContextRoot()));
	}
}

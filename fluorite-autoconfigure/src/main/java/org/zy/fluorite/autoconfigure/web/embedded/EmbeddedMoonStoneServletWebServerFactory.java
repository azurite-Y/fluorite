package org.zy.fluorite.autoconfigure.web.embedded;

import java.util.stream.Collectors;

import org.zy.fluorite.autoconfigure.web.server.moonstone.MoonStoneServletWebServerFactory;
import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.MoonStoneConnectorCustomizer;
import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.MoonStoneContextCustomizer;
import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.MoonStoneProtocolHandlerCustomizer;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnClass;
import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.annotation.Configuration;
import org.zy.fluorite.core.interfaces.function.ObjectProvider;

/**
 * @dateTime 2022年12月10日;
 * @author zy(azurite-Y);
 * @description
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(type = {"javax.servlet.Servlet", "org.zy.moonStone.core.startup.MoonStone"})
public class EmbeddedMoonStoneServletWebServerFactory {
	
	/**
	 * 注册 MoonStoneServletWebServerFactory bean, 并添加bean容器内MoonStoneConnectorCustomizer、
	 * MoonStoneContextCustomizer和MoonStoneProtocolHandlerCustomizer实例到 MoonStoneServletWebServerFactory 中
	 * 
	 */
	@Bean
	MoonStoneServletWebServerFactory moonStoneServletWebServerFactory( ObjectProvider<MoonStoneConnectorCustomizer> connectorCustomizers,
			ObjectProvider<MoonStoneContextCustomizer> contextCustomizers, ObjectProvider<MoonStoneProtocolHandlerCustomizer<?>> protocolHandlerCustomizers) {
		MoonStoneServletWebServerFactory factory = new MoonStoneServletWebServerFactory();
		factory.getMoonStoneConnectorCustomizers().addAll(connectorCustomizers.orderedStream().collect(Collectors.toList()));
		factory.getMoonStoneContextCustomizers().addAll(contextCustomizers.orderedStream().collect(Collectors.toList()));
		factory.getMoonStoneProtocolHandlerCustomizers().addAll(protocolHandlerCustomizers.orderedStream().collect(Collectors.toList()));
		return factory;
	}
	
}

package org.zy.fluorite.autoconfigure.web.embedded;

import java.util.stream.Collectors;

import org.zy.fluorite.autoconfigure.web.server.moonstone.MoonstoneServletWebServerFactory;
import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.MoonstoneConnectorCustomizer;
import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.MoonstoneContextCustomizer;
import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.MoonstoneProtocolHandlerCustomizer;
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
@ConditionalOnClass(type = {"org.zy.moonstone.core.startup.Moonstone"})
public class EmbeddedMoonstoneServletWebServerFactory {
	
	/**
	 * 注册 MoonStoneServletWebServerFactory bean, 并添加bean容器内MoonStoneConnectorCustomizer、
	 * MoonStoneContextCustomizer和MoonStoneProtocolHandlerCustomizer实例到 MoonStoneServletWebServerFactory 中
	 * 
	 */
	@Bean
    MoonstoneServletWebServerFactory moonStoneServletWebServerFactory(ObjectProvider<MoonstoneConnectorCustomizer> connectorCustomizers,
																	  ObjectProvider<MoonstoneContextCustomizer> contextCustomizers,
                                                                      ObjectProvider<MoonstoneProtocolHandlerCustomizer<?>> protocolHandlerCustomizers) {
		MoonstoneServletWebServerFactory factory = new MoonstoneServletWebServerFactory();
		factory.getMoonStoneConnectorCustomizers().addAll(connectorCustomizers.orderedStream().collect(Collectors.toList()));
		factory.getMoonStoneContextCustomizers().addAll(contextCustomizers.orderedStream().collect(Collectors.toList()));
		factory.getMoonStoneProtocolHandlerCustomizers().addAll(protocolHandlerCustomizers.orderedStream().collect(Collectors.toList()));
		return factory;
	}
	
}

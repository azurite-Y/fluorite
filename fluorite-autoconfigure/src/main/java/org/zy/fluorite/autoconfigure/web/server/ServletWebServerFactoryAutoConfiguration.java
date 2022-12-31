package org.zy.fluorite.autoconfigure.web.server;

import org.zy.fluorite.autoconfigure.web.ServerProperties;
import org.zy.fluorite.autoconfigure.web.embedded.EmbeddedMoonStoneServletWebServerFactory;
import org.zy.fluorite.autoconfigure.web.servlet.customizer.MoonStoneServletWebServerFactoryCustomizer;
import org.zy.fluorite.autoconfigure.web.servlet.customizer.ServletWebServerFactoryCustomizer;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnClass;
import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.annotation.Configuration;
import org.zy.fluorite.core.annotation.Import;
import org.zy.fluorite.core.annotation.Order;
import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @dateTime 2022年12月8日;
 * @author zy(azurite-Y);
 * @description servlet web服务器的自动配置
 * WebServerFactoryCustomizer
 */
@Configuration(proxyBeanMethods = false)
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass( type = {"javax.servlet.ServletRequest"} )
@Import( value= {ServletWebServerBeanPostProcessorsRegistrar.class,  EmbeddedMoonStoneServletWebServerFactory.class})
public class ServletWebServerFactoryAutoConfiguration {
	
	@Bean
	public ServletWebServerFactoryCustomizer servletWebServerFactoryCustomizer(ServerProperties serverProperties) {
		return new ServletWebServerFactoryCustomizer(serverProperties);
	}

	@Bean
	@ConditionalOnClass(type = "org.zy.moonStone.core.startup.MoonStone")
	public MoonStoneServletWebServerFactoryCustomizer moonStoneServletWebServerFactoryCustomizer(ServerProperties serverProperties) {
		return new MoonStoneServletWebServerFactoryCustomizer(serverProperties);
	}
	
}

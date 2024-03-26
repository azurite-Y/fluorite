package org.zy.fluorite.autoconfigure.web.embedded;

import javax.servlet.MultipartConfigElement;

import org.zy.fluorite.autoconfigure.web.ServerProperties;
import org.zy.fluorite.autoconfigure.web.servlet.customizer.MoonstoneWebServerFactoryCustomizer;
import org.zy.fluorite.context.annotation.EnableConfigurationProperties;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnClass;
import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.annotation.Configuration;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description 自 /fluorite-autoconfigure/src/main/resources/META-INF/fluorite.factories 加载
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ServerProperties.class)
@ConditionalOnClass(type = { "org.zy.moonstone.core.startup.Moonstone" })
public class EmbeddedWebServerFactoryCustomizerAutoConfiguration {

	/**
	 * 内嵌 MoonStone 配置
	 */
	@Configuration(proxyBeanMethods = false)
	public static class MoonStoneWebServerFactoryCustomizerConfiguration {

		@Bean
		public MoonstoneWebServerFactoryCustomizer moonstoneWebServerFactoryCustomizer(ServerProperties serverProperties, MultipartConfigElement multipartConfigElement) {
			return new MoonstoneWebServerFactoryCustomizer(serverProperties, multipartConfigElement);
		}

	}
}

package org.zy.fluorite.autoconfigure.web.embedded;

import javax.servlet.MultipartConfigElement;

import org.zy.fluorite.autoconfigure.web.ServerProperties;
import org.zy.fluorite.autoconfigure.web.servlet.customizer.MoonStoneWebServerFactoryCustomizer;
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
public class EmbeddedWebServerFactoryCustomizerAutoConfiguration {

	/**
	 * 内嵌 MoonStone 配置
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(type = { "org.zy.moonStone.core.startup.MoonStone" })
	public static class MoonStoneWebServerFactoryCustomizerConfiguration {

		@Bean
		public MoonStoneWebServerFactoryCustomizer moonStoneWebServerFactoryCustomizer(ServerProperties serverProperties, MultipartConfigElement multipartConfigElement) {
			return new MoonStoneWebServerFactoryCustomizer(serverProperties, multipartConfigElement);
		}

	}
}

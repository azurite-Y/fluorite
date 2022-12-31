package org.zy.fluorite.autoconfigure.web.servlet;

import javax.servlet.MultipartConfigElement;

import org.zy.fluorite.context.annotation.EnableConfigurationProperties;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnClass;
import org.zy.fluorite.core.annotation.Autowired;
import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.annotation.Configuration;

/**
 * @dateTime 2022年12月30日;
 * @author zy(azurite-Y);
 * @description
 */
@Configuration
@ConditionalOnClass( type = {"javax.servlet.Servlet"} )
@EnableConfigurationProperties(MultipartProperties.class)
public class MultipartAutoConfiguration {
	@Autowired
	private MultipartProperties multipartProperties;

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		return new MultipartConfigElement(multipartProperties.getLocation(), multipartProperties.getMaxFileSize(), multipartProperties.getMaxRequestSize(),
				(int) multipartProperties.getFileSizeThreshold());
	}
}

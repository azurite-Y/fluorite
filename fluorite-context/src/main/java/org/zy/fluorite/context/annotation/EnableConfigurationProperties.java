package org.zy.fluorite.context.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.zy.fluorite.core.annotation.ConfigurationProperties;
import org.zy.fluorite.core.annotation.Import;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description
 */
@Import(EnableConfigurationPropertiesRegistrar.class)
public @interface EnableConfigurationProperties {
	/**
	 * 快速注册 {@link ConfigurationProperties @ConfigurationProperties} 注解bean的方便方法。
	 * 
	 * @return {@code @ConfigurationProperties} 注解bean注册
	 */
	Class<?>[] value() default {};
}

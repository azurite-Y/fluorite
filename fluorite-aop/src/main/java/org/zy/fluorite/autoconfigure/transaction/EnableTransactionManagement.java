package org.zy.fluorite.autoconfigure.transaction;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.zy.fluorite.core.annotation.Import;

/**
 * @DateTime 2021年9月16日;
 * @author zy(azurite-Y);
 * @Description 标注于配置类上则开启事务
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableTransactionAutoConfiguration
@Import(DataSourceConfiguration.class)
public @interface EnableTransactionManagement {}

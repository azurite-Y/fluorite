package org.zy.fluorite.autoconfigure.transaction;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.zy.fluorite.core.annotation.Import;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
/**
 * @DateTime 2021年9月16日;
 * @author zy(azurite-Y);
 * @Description
 */
@Import({TransactionAutoConfiguration.class,DataSourceTransactionManagerAutoConfiguration.class})
public @interface EnableTransactionAutoConfiguration {}

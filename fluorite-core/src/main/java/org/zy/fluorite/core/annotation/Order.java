package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @DateTime 2020年6月18日 下午2:56:24;
 * @author zy(azurite-Y);
 * @Description
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Order {
	int value() default Ordered.LOWEST_PRECEDENCE;
}

package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午10:55:39;
 * @Description
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lazy {
	/**
	 * 是否需要懒加载
	 * @return
	 */
	boolean value() default true;
}

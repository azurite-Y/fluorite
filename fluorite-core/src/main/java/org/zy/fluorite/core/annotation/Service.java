package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月15日 下午5:27:21;
 * @Description 服务模式注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {
	/**
	 * 别名定义
	 * @return
	 */
	String value() default "";
}

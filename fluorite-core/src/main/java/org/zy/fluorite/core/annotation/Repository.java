package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月15日 下午5:26:41;
 * @Description 数据仓库模式注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Repository {
	/**
	 * 别名定义
	 * @return
	 */
	String value() default "";
}

package org.zy.fluorite.context.annotation.conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年6月29日 下午11:52:42;
 * @author zy(azurite-Y);
 * @Description 类路径是否有指定的资源
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnResourceCondition.class)
public @interface ConditionalOnResource {
	/**
	 * 必须存在的资源。接收资源的相对路径，不支持'classpath前缀'。若指定路径上存在资源则条件成立。
	 */
	String[] resources() default {};
}

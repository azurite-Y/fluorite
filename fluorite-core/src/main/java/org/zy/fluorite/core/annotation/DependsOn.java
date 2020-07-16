package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年6月23日 下午5:22:45;
 * @author zy(azurite-Y);
 * @Description 在当前Bean不显式依赖另一个Bean即另一个Bean不为当前Bean的自动注入属性或构造器参数时，
 * 使用此注解非显式的指定当前Bean所依赖的Bean。可依赖控制Bean的创建顺序
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DependsOn {
	/**
	 * 依赖Bean的beanName数组
	 * @return
	 */
	String[] value() default {};
}

package org.zy.fluorite.boot.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.zy.fluorite.core.annotation.ComponentScan;
import org.zy.fluorite.core.annotation.ComponentScan.Filter;
import org.zy.fluorite.core.annotation.Configuration;
import org.zy.fluorite.core.annotation.filter.AnnotationTypeFilter;
import org.zy.fluorite.core.annotation.filter.FilterType;

/**
 * @DateTime 2020年6月24日 上午10:07:19;
 * @author zy(azurite-Y);
 * @Description 仅且用于标注于根启动类的注解，一个项目的启动需全权依赖于此注解。
 * 类似于SpringBoot的@SpringBootApplication注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {@Filter(type = FilterType.CUSTOM, value = AnnotationTypeFilter.class) })
public @interface RunnerAs {
	/**
	 * 设置此组件的名称，默认为"".
	 * @return
	 */
	String value() default "";
	
	/**
	 * 是否开启debug模式，默认为false则不开启debug模式
	 * @return
	 */
	boolean debug() default false;
	
	/**
	 * 是否开启特定于Aop模块debug模式，默认为false则不开启debug模式
	 * @return
	 */
	boolean debugFormAop() default false;
	
	/**
	 * 是否开启特定于Transaction模块debug模式，默认为false则不开启debug模式
	 * @return
	 */
	boolean debugFromTransaction() default false;
}

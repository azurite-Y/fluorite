package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月14日 下午11:10:10;
 * @Description 指示“lookup”方法，目标bean的解析可以基于返回类型（getBean（Class））或建议的bean名称（getBean（String）），
 * 在这两种情况下，都会将方法的参数传递给getBean调用，以便将它们作为目标工厂方法参数或构造函数参数应用
 */
public @interface Lookup {
	/**
	 * 描述标注方法返回的Bean的beanName，若未指定则是有标注方法的返回值类型关联Bean
	 * @return
	 */
	String value() default "";
}

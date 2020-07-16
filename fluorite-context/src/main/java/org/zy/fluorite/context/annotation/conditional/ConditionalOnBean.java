package org.zy.fluorite.context.annotation.conditional;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年6月29日 下午11:49:06;
 * @author zy(azurite-Y);
 * @Description 当容器里有指定Bean的条件下为true
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBeanCondition.class)
public @interface ConditionalOnBean {
	/**
	 * 检查的bean的类类型。当所有指定类的bean都包含在BeanFactory中时，条件匹配
	 */
	Class<?>[] value() default {};

	/**
	 * 检查的bean的全称类名。当BeanFactory中包含所有指定类的bean时，条件匹配。
	 */
	String[] type() default {};

	/**
	 * 修饰应检查的bean的注释类型(从属于类的注解)。当所有指定的注释都在BeanFactory中的bean上定义时，条件匹配。<br/>
	 * 此条件需配合检查beanName才有效
	 */
	Class<? extends Annotation>[] annotation() default {};

	/**
	 * 要检查的bean的名称。当所有指定的bean名称都包含在BeanFactory中时，条件匹配
	 */
	String[] name() default {};
}

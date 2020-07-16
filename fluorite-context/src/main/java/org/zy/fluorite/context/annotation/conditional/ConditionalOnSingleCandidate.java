package org.zy.fluorite.context.annotation.conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年6月29日 下午11:53:15;
 * @author zy(azurite-Y);
 * @Description 指定的类在BeanFactory中只有一个候选的bean，或者有多个候选的bean，但是其中一个指定了primary
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBeanCondition.class)
public @interface ConditionalOnSingleCandidate {
	/**
	 * 检查的bean的类类型。当所有指定类的bean都包含在BeanFactory中时，条件匹配
	 */
	Class<?>[] value() default {};

	/**
	 * 检查的bean的全称类名。当BeanFactory中包含所有指定类的bean时，条件匹配。
	 */
	String[] type() default {};
}

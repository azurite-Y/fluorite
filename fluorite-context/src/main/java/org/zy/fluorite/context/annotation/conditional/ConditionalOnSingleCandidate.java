package org.zy.fluorite.context.annotation.conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年6月29日 下午11:53:15;
 * @author zy(azurite-Y);
 * @Description
 * 只有当指定类的bean已经包含在BeanFactory中并且可以确定单个候选时才匹配。
 * <p>
 * 这个条件只能匹配到目前为止已经被应用程序上下文处理过的bean定义，因此，强烈建议只在自动配置类上使用这个条件。
 * 如果候选bean可能由另一个自动配置创建，请确保使用此条件的bean在后面运行。
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

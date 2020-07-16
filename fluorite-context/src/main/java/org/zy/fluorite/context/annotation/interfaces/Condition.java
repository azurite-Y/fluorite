package org.zy.fluorite.context.annotation.interfaces;

import org.zy.fluorite.context.annotation.ConditionEvaluator;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnBean;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnClass;
import org.zy.fluorite.core.subject.AnnotationAttributes;

/**
 * @DateTime 2020年6月20日 下午1:13:47;
 * @author zy(azurite-Y);
 * @Description 要注册组件，必须匹配的单个条件
 */
@FunctionalInterface
public interface Condition {
	/**
	 * 因为 {@linkplain Condition} 的实现类一般是由诸如 {@linkplain ConditionalOnBean}
	 * 或 {@linkplain ConditionalOnClass} 这样的条件注解所引入。<br/>
	 * 在{@linkplain ConditionEvaluator}中，同一个处理周期就意味着可能要处理多个 {@link Conditional} 注解。
	 * 而由其引入的 {@linkplain Condition} 实现类又不可控的可能出现重复。<br/>
	 * 所以特规定：同一个处理周期中每个{@linkplain Condition} 实现只会被调用一次matches方法。 <br/>
	 * 综上所述在此方法中需积极的解析其处理的注解，才能有效的完成条件判断任务。
	 * @param context    - 条件上下文
	 * @param attributes - 类注解或方法直接的AnnotationAttributes对象。根据从属不同而此方法匹配的意义也不尽相同。
	 * @return 如果条件匹配并且组件可以注册，则为true；如果要否决带注释组件的注册，则为false
	 */
	boolean matcher(ConditionContext context, AnnotationAttributes attributes);
}

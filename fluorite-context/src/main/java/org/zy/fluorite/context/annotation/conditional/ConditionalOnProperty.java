package org.zy.fluorite.context.annotation.conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年6月29日 下午11:50:17;
 * @author zy(azurite-Y);
 * @Description 指定的属性是否有指定的值
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnPropertyCondition.class)
public @interface ConditionalOnProperty {
	/**
	 *应用于每个属性的前缀。一个有效的前缀由一个或多个用点隔开的单词定义，且以“.”结尾（例如：system.").
	 */
	String prefix() default "";

	/**
	 * 要测试的属性的名称。如果定义了前缀，则应用它来计算每个属性的完整键
	 * <p>
	 * 例如：
	 * 前缀： app.config，此属性为 ["shiro","solr"]。<br/>
	 * 那么完整的键就为 ["app.config.shiro","app.config.solr"]
	 * </p>
	 */
	String[] value() ;

	/**
	 * 设置键的预期属性，若配置读取的属性值跟havingValue做比较，
	 * 如果一样则返回true条件成立反之则返回false条件不成立。
	 * 若未指定则对属性值不作要求
	 */
	String havingValue() default "";

	/**
	 * 指定如果未设置属性，条件是否应匹配。默认为false，即若未设置此属性值则条件不匹配。
	 */
	boolean matchIfMissing() default false;
}

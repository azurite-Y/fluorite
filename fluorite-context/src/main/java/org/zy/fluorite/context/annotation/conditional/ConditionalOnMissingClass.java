package org.zy.fluorite.context.annotation.conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年6月29日 下午11:49:45;
 * @author zy(azurite-Y);
 * @Description 当类路径下没有指定类的条件下为true
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnClassCondition.class)
public @interface ConditionalOnMissingClass {
	/**
	 * 必须不存在的类。
	 * @apiNote BUG：可编译通过，但在缺少指定类的环境下运行时，获取此注解会导致ArrayStoreException异常，其本质还是一个 ClassNotFound 异常
	 */
	@Deprecated
	Class<?>[] value() default {};
	
	/**
	 * 必须不存在于类的全限定类名
	 * 实例化此名称若能获得Class对象则为条件不匹配，返回false
	 */
	String[] type() default {};
}

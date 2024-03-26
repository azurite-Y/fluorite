package org.zy.fluorite.context.annotation.conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年6月29日 下午11:49:32;
 * @author zy(azurite-Y);
 * @Description 当类路径下有指定类的条件下为true.
 * <ol>
 * 一般与 maven 中的 {@code <optional>true</optional> } 属性配合，用作项目依赖项条件判断。<br/>
 * 根据是否拥有某个依赖项中所提供的类而做到引入依赖项而使特定于此依赖项的相关配置生效。<br/>
 * </ol>
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnClassCondition.class)
public @interface ConditionalOnClass {
	/**
	 * 必须存在的类。
	 * @apiNote BUG：可编译通过，但在缺少指定类的环境下运行时，获取此注解会导致ArrayStoreException异常，其本质还是一个 ClassNotFound 异常
	 */
	@Deprecated
	Class<?>[] value() default {};

	/**
	 * 必须存在类的全限定类名。
	 * 实例化此名称若不能获得Class对象则为条件不匹配，返回false
	 */
	String[] type() default {};
}

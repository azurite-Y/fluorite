package org.zy.fluorite.context.annotation.conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.zy.fluorite.context.annotation.interfaces.ConfigurationCondition;

/**
 * @DateTime 2020年6月20日 下午1:49:10;
 * @author zy(azurite-Y);
 * @Description 
 * 指示只有当所有指定条件都匹配时，组件才有资格注册。
 * <p>
 * 条件是在注册 BeanDefinition 之前可以通过编程确定的任何状态。
 * <p>
 * {@code @Conditional } 注解可以以以下任何方式使用：
 * <ul>
 * 		<li>作为直接或间接用 {@code @Conditional} 注解的任何类的类级注解，包括 {@code @Configuration} 类</li>
 * 		<li>作为一个元注解，用于组合自定义原型注解</li>
 * 		<li>作为任何 {@code @Bean} 方法的方法级注解</li>
 * </ul>
 * 
 * <p>
 * 如果 {@code @Configuration} 类标记为 {@code @Conditional}，则与该类关联的所有 {@code @Bean} 方法、{@code @Import} 注释和 {@code @ComponentScan} 注释都将受条件约束。
 * <p>
 * 注意：不支持 {@code @Conditional} 注释的继承；不会考虑来自超类或重写方法的任何条件。
 * 为了实现这些语义，{@code @Conditional} 本身未声明为 {@code @Inherited}；此外，任何使用 {@code @Conditional} 元注释的自定义组合注释都不能声明为 {@code @Inherited}。
 * 
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional {
	/**
	 * 必须匹配的条件集合
	 * @return
	 */
	Class<? extends ConfigurationCondition>[] value();
}

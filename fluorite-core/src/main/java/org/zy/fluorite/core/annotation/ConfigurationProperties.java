package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @dateTime 2022年12月8日;
 * @author zy(azurite-Y);
 * @description 用于外部化配置的注释。如果想绑定和验证一些外部属性(例如，从. Properties文件中)，可以将此添加到类定义或@Configuration类中的@Bean方法中。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {
	/**
	 * 可以绑定到此对象的有效属性的前缀。一个有效的前缀由一个或多个用点分隔的单词定义。{@code "acme.system.feature"})。
	 * 
	 * @return 要绑定属性的前缀
	 */
	String prefix() default "";

	/**
	 * 表示忽略无效的字段, 当属性配置错误时，不希望 Fluorite 程序启动失败  故此设置为false.
	 * <p>
	 * 指示绑定到此对象时应忽略无效字段的标志。
	 * 根据所使用的绑定器，Invalid表示无效，通常这意味着字段类型错误(或者不能强制转换为正确类型)的属性。
	 * 
	 * @return 标志值(默认为false)
	 */
	boolean ignoreInvalidFields() default false;

	/**
	 * 表示忽略未知的字段, 默认是true. 当配置的属性没有绑定到 @ConfigurationProperties 这个类时，希望程序报错
	 * <p>
	 * 用于指示绑定到此对象时应忽略未知字段的标志。未知字段可能表示属性中存在错误
	 * 
	 * @return 标志值(默认为 true)
	 * @deprecated 当前属性用途未知，暂且标记为废弃
	 */
	@Deprecated
	boolean ignoreUnknownFields() default true;
}

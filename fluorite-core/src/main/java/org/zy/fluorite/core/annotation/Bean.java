package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月15日 下午4:37:53;
 * @Description 标注于有返回值的非静态方法上，将返回值注册为bean
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {
	/**
	 * 指定Bean的名称，默认为“”。
	 * @return
	 */
	String value() default "";
	
	/**
	 * 当前Bean是否为其他依赖项的候选，默认为true
	 */
	boolean autowireCandidate() default true;

	/**
	 * 初始化方法。不是通常使用，因为可以在Bean注释方法的主体内以编程方式直接调用该方法。
	 */
	String initMethod() default "";

	/**
	 * 销毁方法。在ApplicationContext关闭时被调用。JDBC数据源实现上的close()方法
	 */
	String destroyMethod() default "";
}

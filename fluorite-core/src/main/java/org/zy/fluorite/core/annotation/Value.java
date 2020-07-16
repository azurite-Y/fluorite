package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月14日 下午11:03:55;
 * @Description 字段或方法/构造函数参数级别上的批注，指示受影响参数的默认值表达式。
 *   通常用于表达式驱动的依赖项注入。还支持处理程序方法参数的动态解析
 */
public @interface Value {
	/**
	 * 实际值表达式 - 例如#{systemProperty.myProp}
	 * @return
	 */
	String value();
}

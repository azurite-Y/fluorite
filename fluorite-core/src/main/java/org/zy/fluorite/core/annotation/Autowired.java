package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月14日 下午11:00:11;
 * @Description 将构造函数、字段、setter方法或config方法标记为需自动注入
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD , ElementType.PARAMETER , ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired  {
	
	/**
	 * 注入Bean是否是必须的。true则是必须的，即若注入Bean未存在于容器中则抛出异常。
	 * @return
	 */
	boolean required() default true;
}

package org.zy.fluorite.context.annotation.conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.zy.fluorite.context.annotation.interfaces.Condition;

/**
 * @DateTime 2020年6月20日 下午1:49:10;
 * @author zy(azurite-Y);
 * @Description
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional {
	/**
	 * 必须匹配的条件集合
	 * @return
	 */
	Class<? extends Condition>[] value();
}

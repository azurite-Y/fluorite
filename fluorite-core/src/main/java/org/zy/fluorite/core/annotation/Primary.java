package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年6月18日 下午3:22:04;
 * @author zy(azurite-Y);
 * @Description 指示当多个候选项被限定为自动连接单值依赖项时，应优先考虑bean。
 * 如果候选对象中恰好存在一个“primary”bean，那么它将是自动注入值。
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Primary {}

package org.zy.fluorite.aop.aspectj.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年7月5日 下午11:37:26;
 * @author zy(azurite-Y);
 * @Description 为指定的目标类引入新的属性和方法
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface DeclareParents {

    /** 目标类型表达式 */
    String value();

    /** 定义接口成员的默认实现 */
    Class<?> defaultImpl() default DeclareParents.class;
}

package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月15日 下午5:13:33;
 * @Description 聚合多个组件扫描批注的容器批注。
 * 可以与Java 8对可重复注释的支持结合使用，其中ComponentScan可以简单地在同一方法上声明多次，从而隐式地生成此容器注释。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ComponentScans {
	ComponentScan[] value();
}

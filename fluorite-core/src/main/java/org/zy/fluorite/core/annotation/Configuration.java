package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月15日 下午4:59:50;
 * @Description 标注标注类注册为组件且标记其为配置类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {
	
	/**
	 * 配置类的别名定义
	 * @return
	 */
	String value() default "";
	
	/**
	 * 是否允许Cglib代理标注了@Bean的方法
	 * @return true则代表运行Cglib进行代理，以执行生命周期处理
	 */
	boolean proxyBeanMethods() default true;
	
}

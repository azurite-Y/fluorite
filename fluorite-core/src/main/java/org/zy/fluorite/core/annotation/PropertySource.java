package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年6月20日 下午2:59:55;
 * @author zy(azurite-Y);
 * @Description
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(PropertySources.class)
public @interface PropertySource {
	/**
	 * 指示此属性源的名称。倘若忽略则使用注解标注类的[类名+'-'+属性文件名]代替
	 * 如：<p>
	 * @PropertySource(name= "", value= { "/a/application.properties" })<br/>
	 * public class App {}<p/>
	 * 那么 name 就是“App-application.properties”<br/>
	 */
	String name() default "";

	/**
	 * 指示要加载的属性文件的资源位置
	 * 支持传统的属性文件格式：如"classpath:/com/myco/app.properties"
	 * 不允许使用资源位置通配符（例如.* /*.properties）；每个位置的计算结果必须正好是一个.properties资源
	 * ${…}占位符将根据环境中已注册的任何/所有属性源解析
	 */
	String[] value();

	/**
	 * 指示是否应忽略找不到属性资源，默认为false。倘若未找到配置文件则抛出异常
	 */
	boolean ignoreResourceNotFound() default false;

	/**
	 * 给定资源的特定字符编码，默认“UTF-8”。
	 */
	String encoding() default "UTF-8";
}

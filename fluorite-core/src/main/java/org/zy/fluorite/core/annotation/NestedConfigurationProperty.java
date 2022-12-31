package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @dateTime 2022年12月8日;
 * @author zy(azurite-Y);
 * @description 
 * 
 * 标注此注解，代表标注属性继承属性所在类标注 {@link ConfigurationProperties#prefix()} . 
 * <pre>
 * ConfigurationProperties(prefix = "server", ignoreUnknownFields = true)
 * public class ServerProperties {
 * ...
 * 	
 * {@link @NestedConfigurationProperty }
 *  private final ErrorProperties error = new ErrorProperties();
 * ...
 * }
 * <pre/>
 * 上述在配置属性中为: server.error.XXX
 * 
 * <p>
 * 指示应将@ConfigurationPropertiesobject中的字段视为嵌套类型。这个注释与实际的绑定过程无关，但它被fluorite-boot配置处理器用作一个提示，提示一个字段没有作为单个值绑定。
 * 指定此选项后，将为字段创建嵌套组，并获取其类型。
 * 
 * <p>
 * 这对集合和映射没有影响，因为这些类型是自动识别的。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NestedConfigurationProperty {

}

package org.zy.fluorite.boot.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.zy.fluorite.boot.AutoConfigurationImportSelector;
import org.zy.fluorite.core.annotation.Import;

/**
 * @DateTime 2020年6月29日 下午4:07:54;
 * @author zy(azurite-Y);
 * @Description
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
}

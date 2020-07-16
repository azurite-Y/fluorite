package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年6月22日 上午12:30:45;
 * @author zy(azurite-Y);
 * @Description  为注册的Bean添加额外的说明信息
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Description {
	/**
	 * 要与BeanDefinition关联的文本描述
	 * @return
	 */
	String value();
}

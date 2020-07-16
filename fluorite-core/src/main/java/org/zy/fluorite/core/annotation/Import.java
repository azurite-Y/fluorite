package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @DateTime 2020年6月19日 下午4:36:27;
 * @author zy(azurite-Y);
 * @Description
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import {
	/**
	 * 指定导入组件的Class对象，一经导入则自动注册为组件
	 * @return
	 */
	Class<?>[] value();
}

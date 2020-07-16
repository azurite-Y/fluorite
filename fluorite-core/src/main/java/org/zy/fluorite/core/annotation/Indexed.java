package org.zy.fluorite.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月15日 下午4:45:34;
 * @Description 标注那些注册组件的注解，如@Component、@Repository
 *  、@Service、@Controller、@Configuration。并为之生成路径索引提升下一次包扫描的运行效率
 * <p>
 * 在第一次进行包扫描时根据扫描到的注解生成路径索引，创建存储路径索引的fluorite.component文件。</br>
 * 在下一次运行时优先读取此文件获得路径索引，然后根据路径索引注册组件。
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Indexed {

}

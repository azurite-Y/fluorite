package org.zy.fluorite.autoconfigure.ioc;

import org.zy.fluorite.context.support.AutowiredAnnotationBeanPostProcessor;
import org.zy.fluorite.context.support.CommonAnnotationBeanPostProcessor;
import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.annotation.Import;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;

/**
 * @DateTime 2020年6月30日 下午5:03:40;
 * @author zy(azurite-Y);
 * @Description 自动配置类。<br/>
 *              使用两种方法分别注册 {@link CommonAnnotationBeanPostProcessor} 类
 *              和{@link AutowiredAnnotationBeanPostProcessor}类
 */
@Import(ImportRegistyBeanPostProcessor.class)
public class BeanPostProcessorAutoConfiguration {
	@Bean
	public AutowiredAnnotationBeanPostProcessor createAutowiredAnnotationBeanPostProcessor(ConfigurableEnvironment environment) {
		return new AutowiredAnnotationBeanPostProcessor(environment);
	}
}

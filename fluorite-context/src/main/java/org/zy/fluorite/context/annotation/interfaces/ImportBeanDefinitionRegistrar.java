package org.zy.fluorite.context.annotation.interfaces;

import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.BeanNameGenerator;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;

/**
 * @DateTime 2020年6月21日 上午11:34:26;
 * @author zy(azurite-Y);
 * @Description 接口将由在处理@Configuration类时注册其他BeanDefinition的类实现。
 * 可以将此类型的类提供给@Import注释或者也可以从 {@link #ImportSelector } 返回，且仅此两种使用方式
 */
public interface ImportBeanDefinitionRegistrar {
	/**
	 * 注册BeanDefinition的回调
	 * @param metadata - 使用@Import注解导入此接口实现的Class对象的AnnotationMetadata对象
	 * @param registry
	 * @param importBeanNameGenerator - 注册Bean的beanName生成器
	 */
	default void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,BeanNameGenerator importBeanNameGenerator) {
		registerBeanDefinitions(metadata, registry);
	}

	/**
	 * 注册BeanDefinition的回调，使用默认的 {@link #AnnotationBeanNameGenerator } 生成BeanDefinition的beanName属性
	 * 
	 * @param importForClass - 使用@Import注解导入此接口实现的Class对象的AnnotationMetadata对象
	 * @param registry  
	 */
	default void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
	}
	
	/**
	 * 属性感知方法
	 * @param instance
	 * @param environment
	 * @param registry
	 */
	void invokeAwareMethods(Environment environment, BeanDefinitionRegistry registry);
}

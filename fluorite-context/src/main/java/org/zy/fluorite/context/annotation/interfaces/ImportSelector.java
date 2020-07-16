package org.zy.fluorite.context.annotation.interfaces;

import java.util.Set;

import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.support.SourceClass;
import org.zy.fluorite.context.annotation.ConditionEvaluator;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;

/**
 * @DateTime 2020年6月20日 下午11:58:40;
 * @author zy(azurite-Y);
 * @Description 接口由类型实现，这些类型根据给定的选择条件（通常是一个或多个注释属性）确定应导入哪个@Configuration类。
 */
public interface ImportSelector {
	/**
	 * 获得导入类的Class对象
	 * @param annotationMetadata - 使用@Import注解导入此ImportSelector实现的AnnotationMetadata对象
	 * @param conditionEvaluator - 要使用的条件注解处理器
	 */
	Set<SourceClass> selectImports(AnnotationMetadata annotationMetadata , ConditionEvaluator conditionEvaluator);

	/**
	 * Environment和BeanDefinitionRegistry感知
	 * @param selector
	 * @param environment
	 * @param registry
	 */
	void invokeAwareMethods(Environment environment, ConfigurableListableBeanFactory beanFactory);
}

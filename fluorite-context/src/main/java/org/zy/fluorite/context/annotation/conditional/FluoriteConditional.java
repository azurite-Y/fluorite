package org.zy.fluorite.context.annotation.conditional;

import java.lang.annotation.Annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.context.annotation.interfaces.ConditionContext;
import org.zy.fluorite.context.annotation.interfaces.ConfigurationCondition;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年6月30日 下午2:01:06;
 * @author zy(azurite-Y);
 * @Description
 */
public abstract class FluoriteConditional implements ConfigurationCondition {
	public final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 检查的bean的类类型。当所有指定类的bean都包含在BeanFactory中时，条件匹配
	 * @param values
	 * @param context
	 * @retur 条件匹配或匹配内容为空集则返回true
	 */
	protected boolean matcher(Class<?>[] values, ConditionContext context) {
		if (values.length == 0) {return true;}
		
		String[] beanNamesForType = null;
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		for (Class<?> clz : values) {
			beanNamesForType = beanFactory.getBeanNamesForType(clz);
			// 指定类型的Bean未在容器中
			if (beanNamesForType.length == 0) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 当指定的类全部存在于容器中时，返回true
	 * @param types
	 * @param context
	 * @return 条件匹配或匹配内容为空集则返回true
	 */
	protected boolean matcher(String[] types, ConditionContext context) {
		if (types.length == 0) {return true;}
		
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		String[] beanNamesForType = null;
		Class <?> forName;
		for (String clzName : types) {
			forName = ReflectionUtils.forName(clzName);
			beanNamesForType = beanFactory.getBeanNamesForType(forName);
			// 指定类型的Bean未在容器中
			if (beanNamesForType.length == 0) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 检查的bean的名称。当所有指定的bean名称都包含在BeanFactory中时，条件匹配。<br/>
	 *	而且当所有指定的注释都在BeanFactory中的bean上定义时，条件匹配。如果有的话
	 * @param names
	 * @param annotations
	 * @param context
	 * @return 条件匹配或匹配内容为空集则返回true
	 */
	protected boolean matcher(String[] names , Class<? extends Annotation>[] annotations , ConditionContext context) {
		if (names.length == 0) {return true;}
		
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		AnnotationMetadata metadata = null;
		RootBeanDefinition beanDefinition = null;

		for (String beanName : names) {
			beanDefinition = beanFactory.getBeanDefinition(beanName);
			if (beanDefinition == null) {
				return false;
			}
			
			if (annotations.length > 0) {
				metadata = beanDefinition.getAnnotationMetadata();
				for (Class<? extends Annotation> anno : annotations) {
					if (!metadata.isAnnotatedForClass(anno)) {
						return false;
					}
				}

			}
		}
		return true;
	}
}

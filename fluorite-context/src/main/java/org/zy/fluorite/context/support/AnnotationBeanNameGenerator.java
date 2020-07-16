package org.zy.fluorite.context.support;

import java.beans.Introspector;

import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.BeanNameGenerator;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.annotation.Component;
import org.zy.fluorite.core.annotation.Qualifier;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年6月17日 下午11:36:32;
 * @author zy(azurite-Y);
 * @Description 读取注解获得组件名，若无果则使用全限定类名代替
 */
public class AnnotationBeanNameGenerator implements BeanNameGenerator {

	public static final AnnotationBeanNameGenerator INSTANCE = new AnnotationBeanNameGenerator();
	
	@Override
	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		String beanName = null;
		if (definition != null) {
			beanName = determineBeanNameFromAnnotation(definition);
			if (Assert.hasText(beanName)) {
				// 找到显式bean名称
				return beanName;
			}
		}
		// 生成唯一的默认bean名称
		return buildDefaultBeanName(definition, registry);
	}

	/**
	 * 尝试从类的注解上获得BeanName。如@Component、@ManagedBean、@Named、@Qualifier的value属性
	 * @param definition
	 * @return
	 */
	protected String determineBeanNameFromAnnotation(BeanDefinition definition) {
		Class<?> beanClass = definition.getBeanClass();
		Component annotations = beanClass.getAnnotation(Component.class);
		Qualifier qualifier = beanClass.getAnnotation(Qualifier.class);
		// 优先使用@Qualifier注解的属性值
		String beanName = qualifier== null ? null : qualifier.value() ;
		return beanName != null ? beanName : 
			annotations == null ? null : annotations.value();
	}

	/**
	 * 将类名首字母小写作为beanName
	 * @param definition
	 * @param registry
	 * @return
	 */
	protected String buildDefaultBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		String beanClassName = definition.getBeanClass().getSimpleName();
		Assert.notNull(beanClassName, "'beanClassName'不可为null或空串，by："+definition);
		return Introspector.decapitalize(beanClassName);
	}
}

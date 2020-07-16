package org.zy.fluorite.context.utils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map.Entry;

import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.context.support.ConfigurationClassPostProcessor;
import org.zy.fluorite.core.annotation.Order;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年6月19日 下午3:50:59;
 * @author zy(azurite-Y);
 * @Description 
 */
public class ConfigurationClassUtils {
	public static final String CONFIGURATION_CLASS_FULL = "full";

	public static final String CONFIGURATION_CLASS_LITE = "lite";

	public static final String CONFIGURATION_CLASS_ATTRIBUTE 
		= StringUtils.append(".",ConfigurationClassPostProcessor.class.getName(),"configurationClass");

	public static final String ORDER_ATTRIBUTE
		= StringUtils.append(".",ConfigurationClassPostProcessor.class.getName(),"order");

	/**
	 * 判断当前类是否为配置类，是则返回true
	 * @param beanDef
	 * @return
	 */
	public static boolean isConfigurationClass (BeanDefinition beanDef) {
		return getConfigurationClassAttribute(beanDef) == CONFIGURATION_CLASS_FULL;
	}
	
	public static String getConfigurationClassAttribute (BeanDefinition beanDef) {
		 Object attribute = beanDef.getAttribute(CONFIGURATION_CLASS_ATTRIBUTE);
		return attribute != null ? attribute.toString() : null;
	}

	/**
	 * 从BeanDefinition中的Class对象判断其是否标注了@Configuration注解，
	 * (if) 若标注了则将“full”保存到BeanDefinition的Attribute集合中。
	 * (else if) 若标注了@Import、@Component、@ImportResource、@ComponentScan、@Bean注解的其中之一则会将“lite” 保存到BeanDefinition的Attribute集合中.
	 * (else) 若均为标注以上注解则直接返回false，反之执行了以上任意一条逻辑则在返回true之前接着检查是否标注了@Order注解，若标注了则注解的value值保存到BeanDefinition的Attribute集合中.
	 */
	public static boolean checkConfigurationClassCandidate(BeanDefinition bd) {
		AnnotationMetadata annotationMetadata = bd.getAnnotationMetadata();
		
		AnnotationAttributes annotationAttributesForClass = annotationMetadata.getAnnotationAttributesForClass();
		
		Order order = annotationAttributesForClass.getAnnotation(Order.class);
		if (order != null) {
			bd.setAttribute( ORDER_ATTRIBUTE, order.value() );
		}
		
		for (Entry<Class<? extends Annotation>, List<Annotation>> annotation : annotationAttributesForClass.entrySet()) {
			Class<? extends Annotation> key = annotation.getKey();
			
			if ( key.getSimpleName().equals("Configuration" ) ) {
				bd.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL); break;
			} else if ( key.getSimpleName().equals("Component" )) {
				bd.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE); break;
			}
		}
		return getConfigurationClassAttribute(bd) != null ? true : false ;
	}
}

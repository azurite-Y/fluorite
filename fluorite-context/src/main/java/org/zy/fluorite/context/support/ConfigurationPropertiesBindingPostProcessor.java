package org.zy.fluorite.context.support;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanPostProcessor;
import org.zy.fluorite.beans.factory.support.AbstractBeanFactory;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.beans.support.AnnotationMetadataHolder;
import org.zy.fluorite.context.interfaces.ApplicationContext;
import org.zy.fluorite.context.interfaces.aware.ApplicationContextAware;
import org.zy.fluorite.core.annotation.ConfigurationProperties;
import org.zy.fluorite.core.annotation.NestedConfigurationProperty;
import org.zy.fluorite.core.annotation.PropertySource;
import org.zy.fluorite.core.environment.StandardEnvironment;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.ConversionServiceStrategy;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.interfaces.PriorityOrdered;
import org.zy.fluorite.core.interfaces.instantiation.InitializingBean;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ReflectionUtils;
import org.zy.fluorite.core.utils.StringUtils;

import com.alibaba.fastjson.JSON;

/**
 * @dateTime 2022年12月8日;
 * @author zy(azurite-Y);
 * @description {@link BeanPostProcessor} 将 {@link PropertySource} 绑定到用 {@link ConfigurationProperties } 注释的bean。
 */
public class ConfigurationPropertiesBindingPostProcessor implements BeanPostProcessor, PriorityOrdered, ApplicationContextAware, InitializingBean {
	private final Logger logger = LoggerFactory.getLogger(ConfigurationPropertiesBindingPostProcessor.class);
	
	/**
	 * 此后处理器注册的bean名称
	 */
	public static final String BEAN_NAME = ConfigurationPropertiesBindingPostProcessor.class.getName();

//	private ApplicationContext applicationContext;

	private StandardEnvironment standardEnvironment;
	
	private ConversionServiceStrategy conversionServiceStrategy;

	private Class<ConfigurationProperties> ConfigurationPropertiesClz = ConfigurationProperties.class;
	private Class<NestedConfigurationProperty> nestedConfigurationProperty = NestedConfigurationProperty.class;

	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//		this.applicationContext = applicationContext;
		conversionServiceStrategy = ((AbstractBeanFactory)applicationContext.getAutowireCapableBeanFactory()).getConversionServiceStrategy();
		
		if (applicationContext.getEnvironment() instanceof StandardEnvironment) {
			this.standardEnvironment = (StandardEnvironment)applicationContext.getEnvironment();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 不能使用应用程序上下文的构造函数注入，因为这会导致工厂bean初始化
//		this.registry = (BeanDefinitionRegistry) this.applicationContext.getAutowireCapableBeanFactory();
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, BeanDefinition beanDefinition) throws BeansException {
		AnnotationMetadata annotationMetadata = beanDefinition.getAnnotationMetadata();
		ConfigurationProperties annotationForClass = annotationMetadata.getAnnotationAttributesForClass().getAnnotation(ConfigurationPropertiesClz);
		
		if ( annotationForClass != null && !( BeanFactory.class.isAssignableFrom(beanDefinition.getBeanClass())) ) {
			try {
				parseConfigurationPropertyClass(beanDefinition.getBeanClass(), bean);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bean;
	}

	
	/**
	 * 解析 ConfigurationProperties 标注类
	 * 
	 * @param configurationPropertyClaz - ConfigurationProperties 标注类Class对象
	 * @param currentObject - ConfigurationProperties 标注类实例
	 */
	private void parseConfigurationPropertyClass(Class<?> configurationPropertyClaz, Object currentObject) {
		AnnotationMetadataHolder annotationMetadataHolder = new AnnotationMetadataHolder(configurationPropertyClaz);
		ConfigurationProperties configurationProperties = annotationMetadataHolder.getAnnotationAttributesForClass().getAnnotation(ConfigurationProperties.class);

		String prefix = "";
		boolean ignoreInvalidFields = false;
		if (configurationProperties != null) {
			ignoreInvalidFields = configurationProperties.ignoreInvalidFields();
			prefix = configurationProperties.prefix();
		} 

		if (configurationProperties != null) {
			parseConfigurationPropertyClass(configurationPropertyClaz, prefix, currentObject, ignoreInvalidFields);
		}
	}
	
	/**
	 * 解析 {@link ConfigurationProperties } 和 {@link NestedConfigurationProperty } 标注类
	 * 
	 * @param configurationPropertyClaz - ConfigurationProperties 标注类Class对象或 NestedConfigurationProperty 标注类Class对象
	 * @param parentAttributeName - 上级属性名
	 * @param currentObject - 当前对象
	 * @param ignoreInvalidFields - 是否忽略无效属性
	 * 
	 */
	private void parseConfigurationPropertyClass(Class<?> configurationPropertyClaz, String parentAttributeName, Object currentObject, boolean ignoreInvalidFields)  {
		Field[] fields = configurationPropertyClaz.getDeclaredFields();
		for (Field field : fields) {
			String createPropertiesName = StringUtils.createPropertiesName(field.getName(), "-");
			String propertiesName =parentAttributeName + "." + createPropertiesName;

			if ( field.getAnnotation(nestedConfigurationProperty) != null ) {
				Object nestedConfigurationPropertyClassObj = null;
				try {
					// 内部对象
					nestedConfigurationPropertyClassObj = getNestedConfigurationPropertyClassObj(currentObject, field);

					parseConfigurationPropertyClass(field.getType(), propertiesName, nestedConfigurationPropertyClassObj, ignoreInvalidFields);
				} catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException e1) {
					if (!ignoreInvalidFields) {
						logger.error("反射参数异常, by class: " + field.getType());
						e1.printStackTrace();
					}
				} catch (InvocationTargetException e1) {
					if (!ignoreInvalidFields) {
						e1.printStackTrace();
					}
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (InstantiationException e1) {
					if (!ignoreInvalidFields) {
						logger.error("无法实例化类, by class: " + field.getType());
						e1.printStackTrace();
					}
				} catch (IntrospectionException e1) {
					if (!ignoreInvalidFields) {
						logger.error("无法获得类getter或setter, by class: " + field.getType());
						e1.printStackTrace();
					}
				}
				
				continue;
			}
			String propertieValue = (String) this.standardEnvironment.getProperty(propertiesName);
//			if (logger.isDebugEnabled()) {
//				logger.debug("Binding Test. propertiesName: '{}'", propertiesName);
//			}
			if (Assert.hasText(propertieValue)) {
				try {
					propertiesSet(field, propertiesName, propertieValue, field.getType(), currentObject);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					if (!ignoreInvalidFields) {
						logger.error("反射参数异常, by class: {}, propertiesName: {}" , field.getType(), propertiesName);
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 反射获取指定参数对象在 currentObject 中是否有值，有则返回，无则通过空构造器构造初始值
	 * 
	 * @param currentObject - 当前处理对象
	 * @param filed - 当前对象属性
	 * @return 当前对象属性类型的反射结果
	 * 
	 * @throws IntrospectionException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InstantiationException 
	 */
	private Object getNestedConfigurationPropertyClassObj(Object currentObject, Field filed) throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
		Class<? extends Object> clz = currentObject.getClass();
		PropertyDescriptor propertyDescriptor;
		propertyDescriptor = new PropertyDescriptor(filed.getName(), clz);

		// getter 方法
		Object nestedClazObj = propertyDescriptor.getReadMethod().invoke(currentObject);

		if (nestedClazObj == null) {
			Constructor<?> declaredConstructor = filed.getType().getDeclaredConstructor();
			nestedClazObj = declaredConstructor.newInstance();
			// setter 方法
			propertyDescriptor.getWriteMethod().invoke(currentObject, nestedClazObj);
		}

		return nestedClazObj;
	}
	
	/**
	 * 属性值设置
	 * 
	 * @param field - 要设置属性值的属性对象
	 * @param propertiesName - 设置属性对应properties文件中的key
	 * @param propertieValue
	 * @param propertieVlaueClaz
	 * @param currentObject
	 * 
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void propertiesSet(Field field, String propertiesName, String propertieValue,Class<?> propertieVlaueClaz, Object currentObject) throws IllegalArgumentException, IllegalAccessException {
		ReflectionUtils.makeAccessible(field);
		Object fieldObject = null;
		if (StringUtils.isJSONValue(propertieValue)) {
			fieldObject = JSON.parseObject(propertieValue, propertieVlaueClaz);
		} else {
			fieldObject = conversionServiceStrategy.convert(propertieValue, propertieVlaueClaz);
		}
		field.set(currentObject, fieldObject);
//		if (logger.isDebugEnabled()) {
//			logger.debug("Property Binding. propertiesName: '{}', propertieValue: '{}'[{}]", propertiesName, fieldObject, fieldObject.getClass().getSimpleName());
//		}

	}
}

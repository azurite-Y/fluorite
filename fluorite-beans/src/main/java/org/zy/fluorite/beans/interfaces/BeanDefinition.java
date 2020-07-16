package org.zy.fluorite.beans.interfaces;

import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.support.ConstructorArgumentValues;
import org.zy.fluorite.beans.support.MutablePropertyValues;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.AttributeAccessor;
import org.zy.fluorite.core.interfaces.BeanMetadataElement;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午4:12:51;
 * @Description
 */
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {
	/**
	 * 设置此bean定义的父定义的名称
	 * @param parentName
	 */
	default void setParentName(String parentName) {}

	/**
	 * 返回此bean定义的父定义的名称（如果有）
	 */
	default String getParentName() {return "";}

	void setQualifiedName(String qualifiedName) ;
	
	String getQualifiedName() ;
	

	/**
	 * 设置当前Bean定义的作用域
	 */
	void setScope(String scope);

	/**
	 * 获得当前Bean定义的作用域
	 */
	String getScope();

	/**
	 * 设置是否应延迟初始化此bean
	 */
	void setLazyInit(boolean lazyInit);

	/**
	 * 判断是否应延迟初始化此bean
	 */
	boolean isLazyInit();

	/**
	 * 设置此bean所依赖的bean的名称
	 */
	void setDependsOn(String... dependsOn);

	/**
	 * 返回此bean所依赖的bean的名称
	 */
	String[] getDependsOn();

	/**
	 * 设置当前bean是否可自动注入其他bean
	 * @param autowireCandidate
	 */
	void setAutowireCandidate(boolean autowireCandidate);

	/**
	 * 判断当前bean是否可自动注入其他bean
	 */
	boolean isAutowireCandidate();

	/**
	 * 设置此bean是否为主要的自动注入候选对象。
	 * 为true则代表在多个额候选中优先使用当前bean进行注入
	 */
	void setPrimary(boolean primary);

	/**
	 * 判断此bean是否为主要的自动注入候选对象
	 */
	boolean isPrimary();
	
	Integer getPriority();
	
	void setPriority(Integer i);

	/**
	 * 指定要使用的FactoryBean实现的bean名称.
	 */
	void setFactoryBeanName(String factoryBeanName);

	/**
	 * 获得要使用的FactoryBean实现的bean名称
	 */
	String getFactoryBeanName();

	/**
	 * 设置要使用的实例化方法名称
	 */
	void setFactoryMethodName(String factoryMethodName);

	/**
	 * 获得要使用的实例化方法名称
	 */
	String getFactoryMethodName();

	/**
	 * 获得实例化bean而调用的构造器参数对象
	 */
	ConstructorArgumentValues getConstructorArgumentValues();

	/**
	 * 判断是否拥有构造器参数
	 * @since 5.0.2
	 */
	default boolean hasConstructorArgumentValues() {
		return !getConstructorArgumentValues().isEmpty();
	}

	/**
	 * 返回要应用于bean的新实例的属性值
	 */
	MutablePropertyValues getPropertyValues();

	/**
	 * 如果有为此bean定义的属性值，则返回。
	 */
	default boolean hasPropertyValues() {
		return !getPropertyValues().isEmpty();
	}

	/**
	 * 设置初始化方法的名称。
	 */
	void setInitMethodName(String initMethodName);

	/**
	 * 获得初始化方法的名称
	 */
	String getInitMethodName();

	/**
	 * 设置销毁方法的名称.
	 * @since 5.1
	 */
	void setDestroyMethodName(String destroyMethodName);

	/**
	 * 获得销毁方法的名称
	 */
	String getDestroyMethodName();

	/**
	 * 设置此bean定义的可读描述。
	 */
	void setDescription(String description);

	/**
	 * 返回此bean定义的可读描述
	 */
	String getDescription();

	/**
	 * 判断bean对象是否是单例的
	 */
	boolean isSingleton();

	/**
	 * 判断bean对象是否是原型对象
	 */
	boolean isPrototype();

	/**
	 * 返回这个bean是否是“抽象的”
	 */
	boolean isAbstract();

	/**
	 * 获得当前Bean调用的克隆对象
	 * @return
	 */
	RootBeanDefinition cloneBeanDefinition();

	default boolean removeInitMethod(String initName) {return false;}
	
	default boolean removeDestroyMethods(String destroyMethod) {return false;}

	/**
	 * 返回组件的Class对象，有些时候此对象不是Bean的Class对象。比如和使用@Bean注解标注的类方法
	 * <p>当FactoryBean实现类注册为组件时，此值存储的是FactoryBean实现类的Class对象</p>
	 * <p>当使用@Bean注解注册Bean时，此值存储的是定义@Bena注解标注方法的类的Class对象</p>
	 * @return
	 */
	Class<?> getBeanClass();
	
	void setBeanClass(Class<?> clz);

	void setBeanName(String beanClassName);
	
	String getBeanName();
	
	AnnotationMetadata getAnnotationMetadata();
	
	void setAnnotationMetadata(AnnotationMetadata annotationMetadata);
	
//	BeanDefinition getOriginatingBeanDefinition();
//	
//	void setRole(int role);
//
//	int getRole();
}

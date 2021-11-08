package org.zy.fluorite.beans.factory.interfaces;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月8日 下午1:31:22;
 * @Description BeanFactory接口的扩展，由bean factorest实现，它可以枚举它们的所有bean实例，而不是按照客户机的请求逐个尝试bean查找名称。
 */
public interface ListableBeanFactory  extends BeanFactory{
	/**
	 * 检查这个bean工厂是否包含具有给定名称的bean定义。
	 * 不考虑此工厂可能参与的任何层次结构，并忽略已通过除bean定义以外的其他方式注册的任何单例bean。
	 */
	boolean containsBeanDefinition(String beanName);

	/**
	 * 返回工厂中BeanDefinition的个数
	 */
	int getBeanDefinitionCount();

	/**
	 * 返回工厂中BeanDefinition的beanName数组
	 */
	List<String> getBeanDefinitionNames();

	/**
	 * 根据类型获得工厂中BeanDefinition的beanName数组
	 */
	String[] getBeanNamesForType(ResolvableType type);

	/**
	 * 根据类型获得工厂中BeanDefinition的beanName数组
	 * @param type
	 * @param includeNonSingletons - 是否包括原型bean或作用域bean，或者仅包括单例bean（也适用于FactoryBeans）
	 * @param allowEagerInit - 是否允许紧急初始化
	 */
	String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit);

	/**
	 * 根据类型获得工厂中BeanDefinition的beanName数组
	 */
	String[] getBeanNamesForType(Class<?> type);

	/**
	 * 根据类型获得工厂中BeanDefinition的beanName数组
	 * @param type
	 * @param includeNonSingletons - 是否包括原型bean或作用域bean，或者仅包括单例bean（也适用于FactoryBeans）
	 * @param allowEagerInit - 是否允许紧急初始化
	 */
	String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);

	/**
	 * 根据类型获得工厂中bean映射集合
	 */
	<T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;

	/**
	 * 根据类型获得工厂中bean映射集合
	 * @param <T>
	 * @param type
	  *@param includeNonSingletons - 是否包括原型bean或作用域bean，或者仅包括单例bean（也适用于FactoryBeans）
	 * @param allowEagerInit - 是否允许紧急初始化
	 * @return
	 * @throws BeansException
	 */
	<T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException;

	/**
	 * 查找用提供的Annotationtype注释的bean的所有名称，而不创建相应的bean实例。
	 * 注意，此方法考虑由FactoryBeans创建的对象，这意味着FactoryBean实现类将被初始化以确定其对象类型
	 */
	String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);

	/**
	 * 查找所有使用所提供的注释类型进行注释的bean，返回bean名称与相应bean实例的映射。
	 * 注意，此方法考虑由FactoryBeans创建的对象，这意味着FactoryBeans将被初始化以确定其对象类型。
	 */
	Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

	/**
	 * 在指定的bean上查找annotationType注释，如果在给定的类本身上找不到注释，则遍历其接口和超级类，并检查bean的工厂方法（如果有）。
	 */
	<A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException;
}

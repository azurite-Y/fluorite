package org.zy.fluorite.beans.factory.interfaces;

import java.util.Set;

import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.support.DependencyDescriptor;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.beans.interfaces.PropertyValues;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午3:20:51;
 * @Description 可自动装配的bean工厂方法定义接口
 */
public interface AutowireCapableBeanFactory extends BeanFactory {
	/**
	 * 创建和初始化指定Class对象的Bean，返回一个原型对象
	 */
	<T> T createBean(Class<T> beanClass) throws BeansException;

	/**
	 * 在实例化后应用回调和bean属性后处理（例如，对于注释驱动的注入），填充给定的bean实例
	 */
	void autowireBean(Object existingBean) throws BeansException;

	/**
	 * 在bean实例化之后对其应用后处理器解析bean依赖和填充相关属性
	 */
	Object configureBean(Object existingBean, String beanName) throws BeansException;

	/**
	 * 创建和初始化bean，并且显示的控制原型bean依赖项自动装配的方法
	 */
	Object createBean(Class<?> beanClass, int autowireMode) throws BeansException;

	/**
	 * 用指定的autowirestrategy实例化给定类的新bean实例
	 */
	Object autowire(Class<?> beanClass, int autowireMode) throws BeansException;

	/**
	 * 按名称或类型自动连接给定bean实例的bean属性。
	 * 也可以使用AUTOWIRE_NO调用，以便仅在实例化回调之后应用（例如，对于注释驱动的注入）
	 */
	void autowireBeanProperties(Object existingBean, int autowireMode)	throws BeansException;

	/**
	 * 初始化给定的原始bean，应用工厂回调，例如setBeanName和setBeanFactory，还应用所有bean后处理器（初始化之前和初始化之后）
	 */
	Object initializeBean(Object existingBean, String beanName) throws BeansException;

	/**
	 * 将MergedBeanDefinitionPostProcessors应用于给定的现有beaninstance
	 * @param mbd
	 * @param beanType
	 * @param beanName
	 */
	void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName);

	/**
	 * 将BeanPostProcessors应用于给定的现有bean实例，在初始化之后调用它们的后处理方法返回的bean实例可以是原始实例的包装器。
	 * @param bean
	 * @param beanName
	 * @return
	 */
	Object applyBeanPostProcessorsBeforeInstantiation(String beanName, RootBeanDefinition mbd);

	/**
	 * 将BeanPostProcessors应用于给定的现有bean实例，在初始化之后调用它们的后处理方法返回的bean实例可以是原始实例的包装器。
	 * @param bean
	 * @param beanName
	 * @return
	 */
	boolean applyBeanPostProcessorsAfterInstantiation(Object bean , BeanDefinition mbd) ;

	/**
	 * 将BeanPostProcessors应用于给定的现有bean实例，在初始化之前调用它们的后处理方法返回的bean实例可以是原始实例的包装器。
	 */
	Object applyBeanPostProcessorsBeforeInitialization(Object bean, BeanDefinition definition)
			throws BeansException;

	/**
	 * 将BeanPostProcessors应用于给定的现有beaninstance，在初始化后调用它们的后处理程序方法返回的bean实例可以是原始实例的包装器。
	 */
	Object applyBeanPostProcessorsAfterInitialization(Object bean, BeanDefinition mbd)
			throws BeansException;

	/**
	 * 检查属性注入，将具有给定名称的bean定义的属性值应用于给定的bean实例。
	 * bean定义可以定义一个完整的自包含bean，重用其属性值，也可以只定义用于现有bean实例的属性值
	 */
	void applyBeanPostProcessorsProperties(PropertyValues pvs, Object bean, BeanDefinition beanDefinition) throws BeansException;

	/**
	 * 销毁给定的bean实例（通常来自createBean）
	 * DestructionAwareBeanPostProcessors后处理器或调用DisposableBean实现类的destroy方法
	 */
	void destroyBean(Object existingBean);

	/**
	 * 为给定的bean名称解析一个bean实例，为目标工厂方法提供一个依赖描述符。
	 */
	Object resolveBeanByName(String name, DependencyDescriptor descriptor) throws BeansException;

	/**
	 * 根据此工厂中定义的bean解析指定的依赖关系
	 */
	Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName,Set<String> autowiredBeanNames) throws BeansException;

}

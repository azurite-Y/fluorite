package org.zy.fluorite.beans.factory.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月12日 下午4:19:09;
 * @Description 负责创建与根bean定义对应的实例的接口。
 */
public interface InstantiationStrategy {

	/**
	 * 返回此工厂中具有给定名称的bean实例
	 */
	Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner)
			throws BeansException;

	/**
	 * 返回此工厂中具有给定名称的bean实例，并通过给定的构造函数创建它。
	 */
	Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner,
			Constructor<?> ctor, Object... args) throws BeansException;

	/**
	 * 返回此工厂中具有给定名称的bean实例，并通过给定工厂方法创建它。
	 */
	Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner,
			Object factoryBean, Method factoryMethod, Object... args)
			throws BeansException;

}

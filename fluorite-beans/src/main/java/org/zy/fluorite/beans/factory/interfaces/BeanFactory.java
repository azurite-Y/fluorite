package org.zy.fluorite.beans.factory.interfaces;

import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy;
 * @DateTime: 2020年6月3日 下午5:03:07;
 * @Description: IOC功能顶级接口之一，规定了基本的Bean工厂所需的功能性方法
 */
public interface BeanFactory {


	/**
	 *  若某Bean由FactoryBean实现类进行实例化，那么在调用getBean(String)方法时其bean名称需以此为前缀
	 */
	String FACTORY_BEAN_PREFIX = "&";

	/**
	 * 通过Bean名称获得其对应的Bean对象
	 * @param name
	 * @return 
	 * @throws BeansException
	 */
	Object getBean(String name) throws BeansException;

	/**
	 * 通过Bean名称获得其对应类型的Bean对象
	 * @param <T>
	 * @param name
	 * @param requiredType
	 * @return
	 * @throws BeansException
	 */
	<T> T getBean(String name, Class<T> requiredType) throws BeansException;

	/**
	 * 通过Bean名称和其实例化所使用的参数集获得对应的Bean对象
	 * @param name
	 * @param args
	 * @return
	 * @throws BeansException
	 */
	Object getBean(String name, Object... args) throws BeansException;

	/**
	 * 返回唯一匹配给定对象类型的bean实例（如果有）
	 * @param <T>
	 * @param requiredType
	 * @return
	 * @throws BeansException
	 */
	<T> T getBean(Class<T> requiredType) throws BeansException;

	<T> T getBean(Class<T> requiredType, Object... args) throws BeansException;

	<T> T getBean(String name, Class<T> requiredType, Object... args);

	/**
	 * 判断指定名称的bean定义是否存在于Bean工厂
	 * @param name
	 * @return
	 */
	boolean containsBean(String name);

	/**
	 * 判断指定Bean名称所代表的Bean是否是单例的，是则返回true
	 * @param name
	 * @return
	 * @throws NoSuchBeanDefinitionException
	 */
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 判断指定Bean名称所代表的Bean是否是原型对象，是则返回true
	 * @param name
	 * @return
	 * @throws NoSuchBeanDefinitionException
	 */
	boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 获得指定名称所代表Bean的类型
	 * @param name
	 * @return bean的类型，如果无法确定，则为null
	 * @throws NoSuchBeanDefinitionException - 如果Bean类型无法确定
	 */
	Class<?> getType(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 获得指定名称所代表Bean的类型
	 * @param name
	 * @param allowFactoryBeanInit - 是否允许FactoryBean实例化
	 * @return bean的类型，如果无法确定，则为null
	 * @throws NoSuchBeanDefinitionException - 如果Bean类型无法确定
	 */
	Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException;

	/**
	 * 检查具有给定名称的bean是否与指定类型匹配
	 * @param beanName
	 * @param type
	 * @param allowEagerInit - 是否急切的实例化对象
	 * @return
	 */
	boolean isTypeMatch(String name, ResolvableType typeToMatch, boolean allowEagerInit);

	/**
	 * 检查具有给定名称的bean是否与指定类型匹配
	 * @param beanName
	 * @param type
	 * @return
	 */
	boolean isTypeMatch(String name, Class<?> typeToMatch);

	/**
	 * 检查具有给定名称的bean是否与指定类型匹配
	 * @param beanName
	 * @param type
	 * @return
	 */
	boolean isTypeMatch(String name, ResolvableType typeToMatch);

	/**
	 * 返回给定bean名称的别名
	 * @param name
	 * @return
	 */
	String[] getAliases(String name);
}

package org.zy.fluorite.beans.factory.interfaces;

import java.util.List;

import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.exception.BeanDefinitionStoreException;
import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.interfaces.AliasRegistry;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月13日 下午1:14:01;
 * @Description
 */
public interface BeanDefinitionRegistry extends AliasRegistry {
	/**
	 * 注册给定名称的BeanDefinition到FactoryBena实现中
	 * @see GenericBeanDefinition
	 * @see RootBeanDefinition
	 */
	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException;

	/**
	 * 删除给定名称的BeanDefinition
	 */
	void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * 通过指定名称获得BeanDefinition对象
	 */
	RootBeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * 判断指定名称的BeanDefinition是否已注册到FactoryBean实现中
	 */
	boolean containsBeanDefinition(String beanName);

	/**
	 * 获得所有注册BeanDefinition的名称集合
	 */
	List<String> getBeanDefinitionNames();

	/**
	 * 获得所有注册BeanDefinition的数量
	 */
	int getBeanDefinitionCount();

	/**
	 * 确定给定的bean名称是否已在此注册表中使用，即是否有本地bean或别名已在此名称下注册
	 */
	boolean isBeanNameInUse(String beanName);
}

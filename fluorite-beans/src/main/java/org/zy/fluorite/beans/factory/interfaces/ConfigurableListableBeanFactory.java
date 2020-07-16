package org.zy.fluorite.beans.factory.interfaces;

import java.util.Iterator;

import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.core.exception.BeansException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月8日 下午1:30:49;
 * @Description 由大多数可列表bean实现的配置接口工厂。
 * 在除了ConfigurableBeanFactory之外，它还提供分析和修改bean定义以及预实例化singleton的工具
 */
public interface ConfigurableListableBeanFactory extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {
	/**
	 * 忽略给定的自动注入依赖项类型：例如，字符串
	 */
	void ignoreDependencyType(Class<?> type);

	/**
	 * 忽略自动连接的给定依赖接口
	 * 默认情况下，只有BeanFactoryAware接口是忽略。对于要忽略的其他类型，请为每个类型调用此方法。
	 */
	void ignoreDependencyInterface(Class<?> ifc);

	/**
	 * 注册已解析的依赖项，方便之后根据类型查找候选值
	 */
	void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue);

	RootBeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	Iterator<String> getBeanNamesIterator();

	/**
	 * 清空元数据缓存
	 */
	void clearMetadataCache();

	/**
	 * 冻结所有bean定义，表示已注册的bean定义将不再被修改或后处理。
	 * 这允许工厂积极地缓存bean定义元数据。
	 */
	void freezeConfiguration();

	/**
	 * 返回此工厂的bean定义是否已冻结，即不应进一步修改或后处理
	 */
	boolean isConfigurationFrozen();

	/**
	 * 确保所有非懒加载的单例Bean都被实例化，同时考虑FactoryBean。通常如果需要，在工厂设置结束时调用。
	 */
	void preInstantiateSingletons() throws BeansException;
}

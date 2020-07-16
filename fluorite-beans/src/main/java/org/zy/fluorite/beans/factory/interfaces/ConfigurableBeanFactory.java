package org.zy.fluorite.beans.factory.interfaces;

import java.util.Map;

import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.exception.BeanDefinitionStoreException;
import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanPostProcessor;
import org.zy.fluorite.beans.factory.support.AbstractBeanFactory;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.BeanExpressionResolver;
import org.zy.fluorite.core.interfaces.ConversionServiceStrategy;
import org.zy.fluorite.core.interfaces.SingletonBeanRegistry;
import org.zy.fluorite.core.interfaces.StringValueResolver;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 上午10:12:19;
 * @Description
 */
public interface ConfigurableBeanFactory extends SingletonBeanRegistry, HierarchicalBeanFactory {
	/**
	 * 设置父bean工厂
	 * @param parentBeanFactory
	 * @throws IllegalStateException
	 */
	void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;
	
	/**
	 * 设置加载bean类对象的ClassLoader实现
	 * @param beanClassLoader
	 */
	void setBeanClassLoader(ClassLoader beanClassLoader);
	
	/**
	 * 获得加载bean类对象的ClassLoader实现
	 * @return
	 */
	ClassLoader getBeanClassLoader();
	
	/**
	 * 设置bean表达式解析程序
	 */
	void setBeanExpressionResolver(BeanExpressionResolver resolver);

	/**
	 * 获得bean表达式解析程序
	 */
	BeanExpressionResolver getBeanExpressionResolver();

	/**
	 * 为嵌入值（如批注属性）添加字符串解析器。
	 */
	void addEmbeddedValueResolver(StringValueResolver valueResolver);

	/**
	 * 确定嵌入值解析程序是否已注册到此Bean工厂，并将通过resolveEmbeddedValue（字符串）应用。
	 */
	boolean hasEmbeddedValueResolver();

	/**
	 * 解析给定的嵌入值，例如注入属性
	 */
	String resolveEmbeddedValue(String value);

	/**
	 * 注册bean后处理器
	 */
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

	/**
	 * 获得所有的bean后处理器个数
	 */
	int getBeanPostProcessorCount();

	/**
	 * 注册给定的作用域，由给定的作用域实现支持
	 */
	void registerScope(String scopeName, Scope scope);

	/**
	 * 返回当前注册的所有作用域的名称。
	 * 这将只返回显式注册的作用域。已生成-在诸如“singleton”和“prototype”这样的范围内不会公开
	 */
	String[] getRegisteredScopeNames();

	/**
	 * 返回给定作用域名称的作用域实现（如果有）
	 */
	Scope getRegisteredScope(String scopeName);

	/**
	 * 从给定的其他工厂复制所有相关配置
	 */
	void copyConfigurationFrom(AbstractBeanFactory otherFactory);

	/**
	 * 注册指定bean的别名
	 */
	void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException;

	/**
	 * 判断name指代的bean是否有FactoryBean实现类实例化
	 */
	boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 显示的指定beanName是否正在创建
	 */
	void setCurrentlyInCreation(String beanName, boolean inCreation);

	/**
	 * 判断当前bean是否正在创建
	 */
	boolean isCurrentlyInCreation(String beanName);

	/**
	 * 将dependentBeanName指代的bean注册为beanName的依赖项
	 */
	void registerDependentBean(String beanName, String dependentBeanName);

	/**
	 * 获得所有依赖指定bean的bean名称
	 */
	String[] getDependentBeans(String beanName);

	/**
	 * 获得指定bean的全部依赖bean
	 */
	String[] getDependenciesForBean(String beanName);

	/**
	 * 销毁指定bean
	 */
	void destroyBean(String beanName, Object beanInstance);

	/**
	 * 销毁当前目标作用域中的指定作用域bean（如果有）。
	 * <p>在销毁过程中出现的任何异常都应该被捕获并记录，而不是传播给此方法的调用方</p>
	 */
	void destroyScopedBean(String beanName);

	/**
	 * 销毁所有单例bean
	 */
	void destroySingletons();
	
	boolean containsBeanDefinition(String beanName);
	
	RootBeanDefinition getBeanDefinition(String beanName) throws BeansException;
	
	Map<String, BeanDefinition> getBeanDefinitions() throws BeansException;
	
	ConversionServiceStrategy getConversionServiceStrategy();
}

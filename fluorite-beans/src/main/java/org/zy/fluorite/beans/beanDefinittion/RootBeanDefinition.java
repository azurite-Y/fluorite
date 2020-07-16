package org.zy.fluorite.beans.beanDefinittion;

import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午11:20:21;
 * @Description
 */
@SuppressWarnings("serial")
public class RootBeanDefinition extends AbstractBeanDefinition {

	private String parentName;

	private boolean isFactoryMethodUnique = false;

	/** 包可见字段，存储已解析的Class对象 */
	private volatile Class<?> resolvedTargetType;

	/** 包可见字段，存储工厂方法的返回类型 */
	private volatile Class<?> factoryMethodReturnType;

	/** 下面四个构造函数字段的公共锁 */
	private final Object constructorArgumentLock = new Object();

	/** 包可见字段，存储已解析的构造器或工厂方法 */
	private Executable resolvedConstructorOrFactoryMethod;

	/** 包可见字段，标识构造器参数是否已被解析 */
	private boolean constructorArgumentsResolved = false;

	/** 包可见字段，存储完全解析的构造函数参数. */
	private List<Object> resolvedConstructorArguments;

	/** 包可见字段，存储部分准备的构造函数参数. */
	private List<Object> preparedConstructorArguments;

	/** 下面两个后处理字段的公用锁 */
	private final Object postProcessingLock = new Object();

	/** 包可见字段，标识已应用MergedBeanDefinitionPostProcessor */
	private boolean postProcessed = false;

	/** 包可见字段，标识已应用实例化之前的bean后处理器 */
	private volatile boolean beforeInstantiationResolved;

	/** 外部管理的配置成员 */
	private Set<Member> configMembers;

	/** 外部管理的初始化方法 */
	private Set<String> initMethods = new HashSet<>();

	/** 外部管理的销毁方法 */
	private Set<String> destroyMethods = new HashSet<>();

	private boolean isFactoryBean;

	/** 存储目标Bean的Class对象，与beanClass所区分 */
	private volatile Class<?> targetType;

	public RootBeanDefinition() {
		super();
	}

	public RootBeanDefinition(Class<?> beanClass) {
		super(beanClass);
	}

	public RootBeanDefinition(RootBeanDefinition rootBeanDefinition) {
		super(rootBeanDefinition);
		this.factoryMethodReturnType = rootBeanDefinition.factoryMethodReturnType;
		this.isFactoryMethodUnique = rootBeanDefinition.isFactoryMethodUnique;
		this.resolvedConstructorOrFactoryMethod = rootBeanDefinition.resolvedConstructorOrFactoryMethod;
		this.setBeanClass(rootBeanDefinition.getBeanClass());
		this.annotationMetadata = rootBeanDefinition.annotationMetadata;
	}

	public RootBeanDefinition(BeanDefinition BeanDefinition) {
		super(BeanDefinition);
	}

	public RootBeanDefinition(Class<?> beanClass, int autowireMode) {
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
	}

	/**
	 * 返回Class对象，有些时候此对象不是Bean的Class对象。比如和使用@Bean注解标注的类方法
	 * <p>
	 * 当FactoryBean实现类注册为组件时，此值存储的是FactoryBean实现类的Class对象
	 * </p>
	 * <p>
	 * 当使用@Bean注解注册Bean时，此值存储的是定义@Bena注解标注方法的类的Class对象
	 * </p>
	 * 
	 * @return
	 */
	public Class<?> getTargetType() {
		if (this.resolvedTargetType != null) {
			return this.resolvedTargetType;
		}

		return this.targetType != null ? this.targetType : this.getBeanClass();
	}

	public void setUniqueFactoryMethodName(String name) {
		Assert.hasText(name, "工厂方法不能为空串");
		setFactoryMethodName(name);
		this.isFactoryMethodUnique = true;
	}

	/**
	 * 判断指定Method是否是当前Bean的工厂方法
	 * 
	 * @param candidate
	 * @return
	 */
	public boolean isFactoryMethod(Method candidate) {
		return candidate.getName().equals(super.getFactoryMethodName());
	}

	/**
	 * 注册注入成员
	 * 
	 * @param configMember
	 */
	public void registerConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			if (this.configMembers == null) {
				this.configMembers = new HashSet<>(1);
			}
			this.configMembers.add(configMember);
		}
	}

	/**
	 * 是否是已注册的注入成员
	 * 
	 * @param configMember
	 * @return
	 */
	public boolean isConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			return (this.configMembers != null && this.configMembers.contains(configMember));
		}
	}

	/**
	 * 注册初始化方法
	 * 
	 * @param initMethod
	 */
	public void registerInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			this.initMethods.add(initMethod);
		}
	}

	/**
	 * 是否是已注册的init方法
	 * 
	 * @param initMethod
	 * @return
	 */
	public boolean isInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			return (this.initMethods != null && this.initMethods.contains(initMethod));
		}
	}

	/**
	 * 注册销毁方法
	 * 
	 * @param configMember
	 */
	public void registerDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			this.destroyMethods.add(destroyMethod);
		}
	}

	/**
	 * 是否是已注册到销毁方法
	 * 
	 * @param destroyMethod
	 * @return
	 */
	public boolean isDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			return (this.destroyMethods != null && this.destroyMethods.contains(destroyMethod));
		}
	}

	public boolean removeInitMethod(String initName) {
		synchronized (this.postProcessingLock) {
			return this.initMethods.remove(initName);
		}
	}
	
	public boolean removeDestroyMethods(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			return this.destroyMethods.remove(destroyMethod);
		}
	}

	@Override
	public RootBeanDefinition cloneBeanDefinition() {
		return new RootBeanDefinition(this);
	}

	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof RootBeanDefinition && super.equals(other)));
	}

	@Override
	public String toString() {
		return "RootBeanDefinition [beanClass=" + beanClass + ", beanName=" + beanName + ", qualifiedName="
				+ qualifiedName + "，initMethods=" + initMethods + ", destroyMethods=" + destroyMethods + "]";
	}

	// getter、setter
//	public boolean isAllowCaching() {
//		return allowCaching;
//	}
//	public void setAllowCaching(boolean allowCaching) {
//		this.allowCaching = allowCaching;
//	}
	public boolean isFactoryMethodUnique() {
		return isFactoryMethodUnique;
	}

	public void setFactoryMethodUnique(boolean isFactoryMethodUnique) {
		this.isFactoryMethodUnique = isFactoryMethodUnique;
	}

	public Class<?> getResolvedTargetType() {
		return resolvedTargetType;
	}

	public void setResolvedTargetType(Class<?> resolvedTargetType) {
		this.resolvedTargetType = resolvedTargetType;
	}

	public Class<?> getFactoryMethodReturnType() {
		return factoryMethodReturnType;
	}

	public void setFactoryMethodReturnType(Class<?> factoryMethodReturnType) {
		this.factoryMethodReturnType = factoryMethodReturnType;
	}

	public Executable getResolvedConstructorOrFactoryMethod() {
		return resolvedConstructorOrFactoryMethod;
	}

	public void setResolvedConstructorOrFactoryMethod(Executable resolvedConstructorOrFactoryMethod) {
		this.resolvedConstructorOrFactoryMethod = resolvedConstructorOrFactoryMethod;
	}

	public boolean isConstructorArgumentsResolved() {
		return constructorArgumentsResolved;
	}

	public void setConstructorArgumentsResolved(boolean constructorArgumentsResolved) {
		this.constructorArgumentsResolved = constructorArgumentsResolved;
	}

	public List<Object> getResolvedConstructorArguments() {
		return this.resolvedConstructorArguments;
	}

	public void addResolvedConstructorArguments(Object resolvedConstructorArgument) {
		if (this.resolvedConstructorArguments == null) {
			this.resolvedConstructorArguments = new ArrayList<>();
		}
		this.resolvedConstructorArguments.add(resolvedConstructorArgument);
	}

	public List<Object> getPreparedConstructorArguments() {
		return preparedConstructorArguments;
	}

	public void setPreparedConstructorArguments(Object preparedConstructorArgument) {
		if (this.preparedConstructorArguments == null) {
			this.preparedConstructorArguments = new ArrayList<>();
		}
		this.preparedConstructorArguments.add(preparedConstructorArgument);
	}

	public boolean isPostProcessed() {
		return postProcessed;
	}

	public void setPostProcessed(boolean postProcessed) {
		this.postProcessed = postProcessed;
	}

	public Boolean getBeforeInstantiationResolved() {
		return beforeInstantiationResolved;
	}

	public void setBeforeInstantiationResolved(Boolean beforeInstantiationResolved) {
		this.beforeInstantiationResolved = beforeInstantiationResolved;
	}

	public Set<Member> getConfigMembers() {
		return configMembers;
	}

	public void setConfigMembers(Set<Member> configMembers) {
		this.configMembers = configMembers;
	}

	public Set<String> getInitMethods() {
		return initMethods;
	}

	public void setInitMethods(Set<String> initMethods) {
		this.initMethods = initMethods;
	}

	public Set<String> getDestroyMethods() {
		return destroyMethods;
	}

	public void setDestroyMethods(Set<String> destroyMethods) {
		this.destroyMethods = destroyMethods;
	}

	public Object getConstructorArgumentLock() {
		return constructorArgumentLock;
	}

	public Object getPostProcessingLock() {
		return postProcessingLock;
	}

	public boolean isFactoryBean() {
		return isFactoryBean;
	}

	public void setFactoryBean(boolean isFactoryBean) {
		this.isFactoryBean = isFactoryBean;
	}

	@Override
	public void setParentName(String parentName) {
		this.parentName = parentName;

	}

	@Override
	public String getParentName() {
		return this.parentName;
	}

	@Override
	public Class<?> getBeanClass() {
		return super.beanClass;
	}

	@Override
	public void setBeanClass(Class<?> clz) {
		super.beanClass = clz;
	}

	public void setResolvedConstructorArguments(List<Object> resolvedConstructorArguments) {
		this.resolvedConstructorArguments = resolvedConstructorArguments;
	}

	public void setResolvedConstructorArguments(Object[] resolvedConstructorArguments) {
		this.resolvedConstructorArguments = Arrays.asList(resolvedConstructorArguments);
	}

	public void setPreparedConstructorArguments(List<Object> preparedConstructorArguments) {
		this.preparedConstructorArguments = preparedConstructorArguments;
	}

	public void setTargetType(Class<?> targetType) {
		this.targetType = targetType;
	}

}

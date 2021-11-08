package org.zy.fluorite.context.support;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.zy.fluorite.beans.beanDefinittion.AbstractBeanDefinition;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.exception.BeanCreationException;
import org.zy.fluorite.beans.factory.exception.BeanDefinitionStoreException;
import org.zy.fluorite.beans.factory.exception.BeanDefinitionValidationException;
import org.zy.fluorite.beans.factory.exception.BeanNotOfRequiredTypeException;
import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.MergedBeanDefinitionPostProcessor;
import org.zy.fluorite.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.zy.fluorite.beans.factory.support.AbstractBeanFactory;
import org.zy.fluorite.beans.factory.support.ConstructorResolver;
import org.zy.fluorite.beans.factory.support.DependencyDescriptor;
import org.zy.fluorite.beans.factory.support.InjectionPoint;
import org.zy.fluorite.beans.factory.support.NullBean;
import org.zy.fluorite.beans.interfaces.AutowireCandidateResolver;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.exception.TypeMismatchException;
import org.zy.fluorite.core.interfaces.function.BeanObjectProvider;
import org.zy.fluorite.core.interfaces.function.ObjectFactory;
import org.zy.fluorite.core.interfaces.function.ObjectProvider;
import org.zy.fluorite.core.interfaces.instantiation.FactoryBean;
import org.zy.fluorite.core.interfaces.instantiation.SmartFactoryBean;
import org.zy.fluorite.core.interfaces.instantiation.SmartInitializingSingleton;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.AutowireUtils;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月13日 下午1:10:10;
 * @Description 对ConfigurableListableBeanFactory和BeanDefinitionRegistry接口的默认实现：
 *              一个基于bean定义元数据的成熟bean工厂，可通过后处理器进行扩展。
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

	private String serializationId;

	/** 存储已解析的依赖项 { 依赖项类对象 : 依赖项实例 } */
	private final Map<Class<?>, Object> resolvableDependencies = new ConcurrentHashMap<>(16);

	/** 存储注册的BeanDefinition对象 { beanName : BeanDefinition } */
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

	/** 存储所有Bean名称数组 { Bean类对象 : Bean名称 } */
	private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<>(64);

	/** 存储单例名称数组 { 单例类对象 : 单例Bean名称 } */
	private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<>(64);

	/** 存储已注册的BeanDefinition名称，按照注册顺序排列 */
	private volatile List<String> beanDefinitionNames = new ArrayList<>(256);

	/** 依赖项列表和数组的可选OrderComparator */
	private Comparator<Object> dependencyComparator;

	/** 在冻结配置的情况下缓存的bean定义名称数组 */
	private volatile String[] frozenBeanDefinitionNames;

	/** 是否可以为所有bean缓存bean定义元数据 */
	private volatile boolean configurationFrozen = false;

	public DefaultListableBeanFactory() {
		super();
	}

	/**
	 * @param parentBeanFactory - 父类Bean工厂
	 */
	public DefaultListableBeanFactory(BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}

	@Override
	public void copyConfigurationFrom(AbstractBeanFactory otherFactory) {
		super.copyConfigurationFrom(otherFactory);
		if (otherFactory instanceof DefaultListableBeanFactory) {
			DefaultListableBeanFactory otherListableFactory = (DefaultListableBeanFactory) otherFactory;
			this.dependencyComparator = otherListableFactory.dependencyComparator;
			this.resolvableDependencies.putAll(otherListableFactory.resolvableDependencies);
		}
	}

	@Override
	public void ignoreDependencyType(Class<?> type) {
		Assert.notNull(type, "忽略的依赖项类型不能为null");
		this.ignoredDependencyTypes.add(type);
	}

	@Override
	public void ignoreDependencyInterface(Class<?> ifc) {
		Assert.notNull(ifc, "忽略的依赖项接口不能为null");
		this.ignoredDependencyInterfaces.add(ifc);
	}

	@Override
	public void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue) {
		Assert.notNull(dependencyType, "依赖类型不能为null");
		if (autowiredValue != null) {
			if (!(autowiredValue instanceof ObjectFactory || dependencyType.isInstance(autowiredValue))) {
				throw new IllegalArgumentException(
						"已解析的依赖项注册异常， [" + autowiredValue + "] 还未实现指定的依赖类型 [" + dependencyType.getName() + "]");
			}
			this.resolvableDependencies.put(dependencyType, autowiredValue);
		}
	}

	@Override
	public void clearMetadataCache() {
		clearByTypeCache();
	}

	private void clearByTypeCache() {
		this.singletonBeanNamesByType.clear();
	}

	@Override
	public void freezeConfiguration() {
		this.configurationFrozen = true;
		this.frozenBeanDefinitionNames = StringUtils.toStringArray(this.beanDefinitionNames);
	}

	@Override
	public boolean isConfigurationFrozen() {
		return this.configurationFrozen;
	}

	@Override
	public void preInstantiateSingletons() throws BeansException {
		if (DebugUtils.debug) {
			logger.info("开始实例化单例，by：" + this);
		}

		// 遍历副本以允许init方法注册新的bean定义。虽然这可能不是常规工厂引导的一部分，但它在其他方面工作正常
		List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);
		// 所有非惰性单例bean的触发器初始化
		for (String beanName : beanNames) {
			/**
			 * 在合并的Bean容器中查找给定的bean实例名对应的RootBeanDefinition对象 BeanDefinition:
			 * 描述和定义了创建一个bean需要的所有信息，属性，构造函数参数以及访问它们的方法。还有其他一些信息，比如这些定义来源自哪个类等等
			 * #MergedBeanDefinition：基于原始BeanDefinition及其双亲BeanDefinition信息得到一个信息"合并"之后的BeanDefinition，在通过getBeanNamesForType方法时会将BeanName和模型对象的映射保存到此容器中
			 */
			RootBeanDefinition bd = this.getBeanDefinition(beanName);
			/**
			 * 判断此bean建模对象是非抽象、非懒加载的单例对象
			 */
			if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
				/**
				 * 判断BeanName所代表的单例对象或BeanDefinition中的BeanClass是否实现了FactoryBean
				 * 首先判断其BeanName所代表的Bean是否存在于单例集合中，存在则使用instanceof关键字判断是否为FactoryBean实现
				 * 若不存在于单例集合则使用当前BeanFactory的父类检查其BeanName所代表的对象是否为FactoryBean的实现
				 * 若父BeanFactory不存在或未实现ConfigurableBeanFactory接口则通过其BeanDefinition对象的BeanClass属性判断其是否为FactoryBean实现
				 */
				if (isFactoryBean(beanName)) {
					// FACTORY_BEAN_PREFIX = “&”
					Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
					if (bean instanceof FactoryBean) {
						final FactoryBean<?> factory = (FactoryBean<?>) bean;
						/**
						 * SmartFactoryBean为FactoryBean的子接口，其isEagerInit方法默认实现返回false，即FactoryBean实例化的对象进行懒加载
						 * isEagerInit：这个FactoryBean是否期望进行急切的初始化，即急切地初始化自己，以及期待对其singleton对象（如果有的话）进行急切的初始化？
						 */
						boolean isEagerInit = (factory instanceof SmartFactoryBean
								&& ((SmartFactoryBean<?>) factory).isEagerInit());
						if (isEagerInit) {
							// 此处未不加“&”前缀
							getBean(beanName);
						}
					}
				} else {
					getBean(beanName);
				}
			}
		}

		/**
		 * 为所有适用的bean触发初始化后回调
		 */
		for (String beanName : beanNames) {
			Object singletonInstance = getSingleton(beanName);
			if (singletonInstance instanceof SmartInitializingSingleton) {
				final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
				smartSingleton.afterSingletonsInstantiated();
			}
		}

	}

	@Override
	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		Assert.hasText(beanName, "'beanName'不能为null或空串 ");

		BeanDefinition bd = this.beanDefinitionMap.remove(beanName);
		if (bd == null) {
			if (DebugUtils.debug) {
				logger.info("未找到需要删除的BeanDefinition对象，by：" + beanName);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}

		if (super.hasBeanCreationStarted()) {
			// 无法再修改启动时间集合元素（用于稳定迭代）
			synchronized (this.beanDefinitionMap) {
				List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames);
				updatedDefinitions.remove(beanName);
				this.beanDefinitionNames = updatedDefinitions;
			}
		} else {
			// 处于启动注册阶段
			this.beanDefinitionNames.remove(beanName);
		}

		resetBeanDefinition(beanName);

	}

	/**
	 * 重置BeanDefinition
	 * 
	 * @param beanName
	 */
	protected void resetBeanDefinition(String beanName) {
		// 移除给定bean的合并bean定义（如果已经创建）
		clearBeanDefinition(beanName);

		// 从单例缓存中移除相应的bean（如果有的话）
		destroySingleton(beanName);

		// 通知所有后处理器指定的bean定义已重置。
		for (BeanPostProcessor processor : super.beanPostProcessors) {
			if (processor instanceof MergedBeanDefinitionPostProcessor) {
				((MergedBeanDefinitionPostProcessor) processor).resetBeanDefinition(beanName);
			}
		}

		// 重置所有将给定bean作为父bean的bean定义（递归地）
		for (String bdName : this.beanDefinitionNames) {
			if (!beanName.equals(bdName)) {
				BeanDefinition bd = this.beanDefinitionMap.get(bdName);
				// Ensure bd is non-null due to potential concurrent modification
				// of the beanDefinitionMap.
				if (bd != null && beanName.equals(bd.getParentName())) {
					resetBeanDefinition(bdName);
				}
			}
		}
	}

	public void clearBeanDefinition(String beanName) {
		this.beanDefinitionMap.remove(beanName);
	}

	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
		Assert.hasText(beanName, "'beanName'不能为null");
		Assert.notNull(beanDefinition, "'beanDefinition'不能为null，by：" + beanName);

		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				((AbstractBeanDefinition) beanDefinition).validate();
			} catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException("验证BeanDefinition失败，by：" + beanDefinition, ex);
			}
		}
		BeanDefinition existingDefinition = this.getBeanDefinition(beanName);
		if (existingDefinition != null) {
			if (DebugUtils.debug) {
				logger.info("'{}'的BeanDefinition对象再次注册，将由-'{}'覆盖原BeanDefinition对象-'{}'", beanName, existingDefinition,
						beanDefinition);
			}
		} else {
			if (DebugUtils.debug) {
				logger.info("注册'{}'的BeanDefinition对象- {}", beanName, beanDefinition);
			}
			this.beanDefinitionNames.add(beanName);
		}
		this.beanDefinitionMap.put(beanName, beanDefinition);
	}

	@Override
	public boolean isBeanNameInUse(String beanName) {
		return super.isAlias(beanName) || this.containsBeanDefinition(beanName);
	}

	@Override
	public Iterator<String> getBeanNamesIterator() {
		return this.beanDefinitionMap.keySet().iterator();
	}

	@Override
	public RootBeanDefinition getBeanDefinition(String beanName) throws BeansException {
		Assert.hasText(beanName, "'beanName'不能为null");
		BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
		if (beanDefinition != null) {
			return (RootBeanDefinition) beanDefinition;
		}
		//		DebugUtils.log(logger, "未找到指定的BeanDefinition，by name："+beanName);
		//		throw new NoSuchBeanDefinitionException(beanName);
		return null;
	}

	@Override
	public boolean containsBeanDefinition(String beanName) {
		Assert.hasText(beanName, "'beanName'不能为null");
		return this.beanDefinitionMap.containsKey(beanName);
	}

	@Override
	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	@Override
	public List<String> getBeanDefinitionNames() {
		return this.beanDefinitionNames;
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type) {
		/**
		 * 尽可能的让IOC先初始化必要类型的Bean，最后在刷新上下文的时候再初始化FactoryBean
		 */
		return getBeanNamesForType(type, true, false);
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		Class<?> resolved = type.resolve();
		if (resolved != null && !type.hasGenerics()) {
			return getBeanNamesForType(resolved, includeNonSingletons, allowEagerInit);
		} else {
			return doGetBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		}
	}

	@Override
	public String[] getBeanNamesForType(Class<?> type) {
		return getBeanNamesForType(type, true, true);
	}

	@Override
	public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		if (!isConfigurationFrozen() || type == null || !allowEagerInit) {
			return doGetBeanNamesForType(ResolvableType.forClass(type), includeNonSingletons, allowEagerInit);
		}
		// 是否包含非单例Bean
		Map<Class<?>, String[]> cache = (includeNonSingletons ? this.allBeanNamesByType
				: this.singletonBeanNamesByType);
		String[] resolvedBeanNames = cache.get(type);
		if (resolvedBeanNames != null) { // 有结果则返回
			return resolvedBeanNames;
		}
		resolvedBeanNames = doGetBeanNamesForType(ResolvableType.forClass(type), includeNonSingletons, true);
		// 保存映射
		cache.put(type, resolvedBeanNames);
		return resolvedBeanNames;
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		return getBeansOfType(type, true, true);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {
		String[] beanNames = getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		Map<String, T> result = new LinkedHashMap<>(beanNames.length);
		for (String beanName : beanNames) {
			try {
				Object beanInstance = getBean(beanName);
				if (!(beanInstance instanceof NullBean)) {
					result.put(beanName, (T) beanInstance);
				}
			} catch (BeanCreationException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 根据类型获得工厂中BeanDefinition的beanName数组
	 * 
	 * @param type
	 * @param includeNonSingletons - 是否包括原型bean或作用域bean，或者仅包括单例bean（也适用于FactoryBeans）
	 * @param allowEagerInit       - 是否允许紧急初始化
	 */
	private String[] doGetBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		List<String> result = new ArrayList<>();

		for (String beanName : this.beanDefinitionNames) {
			// 只有当bean名称没有定义为其他bean的别名时，才认为bean是合格的
			if (!isAlias(beanName)) {
				try {
					RootBeanDefinition mbd = this.getBeanDefinition(beanName);
					if (!mbd.isAbstract()) {
						// 当前Bean是否由FactoryBean对象实例化
						boolean isFactoryBean = isFactoryBean(beanName, mbd);
						// 当前Bean是否为指定类型的实例
						boolean matchFound = false;
						// 是否非懒加载，为false则为懒加载
						boolean isNonLazyDecorated = mbd != null && !mbd.isLazyInit();
						if (!isFactoryBean) {
							if (includeNonSingletons || isSingleton(beanName, mbd)) {
								matchFound = isTypeMatch(beanName, type, allowEagerInit);
							}
						} else { // FactoryBean实例化
							if (includeNonSingletons || isNonLazyDecorated
									|| (allowEagerInit && isSingleton(beanName, mbd))) {
								matchFound = isTypeMatch(beanName, type, allowEagerInit);
							}
						}
						if (matchFound) {
							result.add(beanName);
						}
					}
				} catch (BeanDefinitionStoreException ex) {
					if (allowEagerInit) {
						ex.printStackTrace();
					}
				}
			}
		}

		return StringUtils.toStringArray(result);
	}

	public boolean isSingleton(String beanName, RootBeanDefinition mbd) {
		return (mbd != null ? mbd.isSingleton() : isSingleton(beanName));
	}

	/**
	 * 解析Bean并创建Bean
	 * 
	 * @param type
	 * @param args
	 * @return
	 */
	@Override
	protected Object resolveBean(ResolvableType type, Object[] args) {
		Class<?> resolveClass = type.resolve();
		String[] candidateNames = getBeanNamesForType(resolveClass);
		if (candidateNames.length > 1) {
			List<String> autowireCandidates = new ArrayList<>(candidateNames.length);
			for (String beanName : candidateNames) {
				if (!containsBeanDefinition(beanName) || getBeanDefinition(beanName).isAutowireCandidate()) {
					autowireCandidates.add(beanName);
				}
			}
			if (autowireCandidates.size() == 1) {
				return getBean(autowireCandidates.get(0), args);
			} else {
				String candidateName = determinePrimaryCandidate(autowireCandidates, resolveClass);
				if (candidateName == null) {
					candidateName = determineHighestPriorityCandidate(autowireCandidates, resolveClass);
				}

				if (candidateName == null) {
					throw new BeansException("同类型的Bean候选不唯一，by：class" + resolveClass);
				} else {
					return getBean(candidateName, args);
				}
			}

		} else {
			return getBean(candidateNames[0], args);
		}
	}

	/**
	 * 挑选出@Priority注解值最小的BeanName
	 * 
	 * @param autowireCandidates
	 * @param resolveClass
	 * @return
	 */
	public String determineHighestPriorityCandidate(List<String> autowireCandidates, Class<?> resolveClass) {
		RootBeanDefinition beanDefinition = null;
		int i = Integer.MAX_VALUE;
		Integer priorityValue = 0;
		String priorityBeanName = null;
		for (String beanName : autowireCandidates) {
			beanDefinition = this.getBeanDefinition(beanName);
			priorityValue = beanDefinition.getPriority();
			if (priorityValue != null && priorityValue <= i) {
				i = priorityValue;
				priorityBeanName = beanName;
			}
		}
		return priorityBeanName;
	}

	/**
	 * 挑选出标注了@Primary注解的BeanName
	 * 
	 * @param autowireCandidates
	 * @param resolveClass
	 * @return
	 */
	public String determinePrimaryCandidate(List<String> autowireCandidates, Class<?> resolveClass) {
		RootBeanDefinition beanDefinition = null;
		for (String beanName : autowireCandidates) {
			beanDefinition = this.getBeanDefinition(beanName);
			if (beanDefinition.isPrimary()) {
				return beanName;
			}
		}
		return null;
	}

	public AutowireCandidateResolver getAutowireCandidateResolver(AbstractAutowireCapableBeanFactory beanFactory) {
		if (super.autowireCandidateResolver == null) {
			super.autowireCandidateResolver = new QualifierAnnotationAutowireCandidateResolver(beanFactory);
		}
		return super.autowireCandidateResolver;
	}

	@Override
	public Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName,
			Set<String> autowiredBeanNames) throws BeansException {
		descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());
		/**
		 * 优先解析其他Bean注入接口
		 */
		Class<?> dependencyType = descriptor.getDependencyType();
		if (ObjectFactory.class == dependencyType || ObjectProvider.class == dependencyType) {
			return new DependencyObjectProvider(descriptor, requestingBeanName);
		} else {
			// 判断是否需要生成懒加载对象
			Object result = getAutowireCandidateResolver(this).getLazyResolutionProxyIfNecessary(descriptor,
					requestingBeanName);
			if (result == null) {
				result = doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames);
			}
			return result;
		}
	}

	@Override
	public Object doResolveDependency(DependencyDescriptor descriptor, String beanName,
			Set<String> autowiredBeanNames) {
		InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
		try {
			Object shortcut = descriptor.resolveShortcut(this);
			if (shortcut != null) {
				return shortcut;
			}
			Class<?> type = descriptor.getDependencyType();
			// 确定是否为给定依赖项建议默认值。默认实现只返回空值
			Object value = getAutowireCandidateResolver(this).getSuggestedValue(descriptor);
			if (value != null) {
				if (value instanceof String) {
					value = resolveEmbeddedValue((String) value);
				}
				if (type.isInstance(value)) {
					return value;
				} else {
					throw new TypeMismatchException("默认值类型转换异常，类型[" + value + "]不可转换为[" + type + "]类型");
				}
			}

			List<String> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					if (BeanFactory.class.isAssignableFrom(type)) {
						return this;
					}
					// 对于无法解析的依赖项，引发BeanNotOfRequiredTypeException
					throw new BeanNotOfRequiredTypeException("无法解析的依赖项，by：" + descriptor.getDependencyType().getName() + " ,beanName：" + beanName);
				}
				return null;
			}

			String autowiredBeanName = null;
			Object instanceCandidate = null;

			if (matchingBeans.size() > 1) {
				// 确定合适的候选，返回最优的beanName
				autowiredBeanName = determineAutowireCandidate(matchingBeans, descriptor);
				if (Assert.hasText(autowiredBeanName)) {
					if (descriptor.isRequired()) {
						// 对于无法解析的依赖项，引发BeanNotOfRequiredTypeException
						throw new BeanNotOfRequiredTypeException(
								"无法解析的依赖项，by：" + descriptor.getDependencyType().getName());
					}
					return null;
				}
				// 从ioc容器中获得此bean
				instanceCandidate = getBean(autowiredBeanName, type);
			} else {
				// 只有一个候选
				autowiredBeanName = matchingBeans.get(0);
				instanceCandidate = getBean(autowiredBeanName);
			}

			if (autowiredBeanNames != null) { // 一切尘埃落定之后将确定的自动注入Bean名称加入到自动注入Bean名称集合中。
				autowiredBeanNames.add(autowiredBeanName);
			}

			if (instanceCandidate instanceof Class) {
				instanceCandidate = getBean(beanName);
			}
			if (instanceCandidate instanceof NullBean) {
				if (descriptor.isRequired()) {
					// 对于无法解析的依赖项，引发BeanNotOfRequiredTypeException
					throw new BeanNotOfRequiredTypeException("无法解析的依赖项，by：" + descriptor.getDependencyType().getName());
				}
				instanceCandidate = null;
			}
			return instanceCandidate;
		} finally {
			ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
		}
	}

	/**
	 * 确定最合适的Bean名称或bean对象
	 * 
	 * @param matchingBeans
	 * @param descriptor
	 * @param autowiredBeanNames - 自动装配的beanName
	 * @return
	 */
	private String determineAutowireCandidate(List<String> candidates, DependencyDescriptor descriptor) {
		Class<?> requiredType = descriptor.getDependencyType();
		String primaryCandidate = determinePrimaryCandidate(candidates, requiredType);
		if (primaryCandidate != null) {
			return primaryCandidate;
		}
		String priorityCandidate = determineHighestPriorityCandidate(candidates, requiredType);
		if (priorityCandidate != null) {
			return priorityCandidate;
		}

		Object autowiringValue = null;
		Class<?> beanClass = null;
		for (String beanName : candidates) {
			beanClass = this.getBeanDefinition(beanName).getBeanClass();
			if (requiredType.isAssignableFrom(beanClass)) {
				autowiringValue = resolvableDependencies.get(beanClass);
				// 获得完整的实例对象，有可能是ObjectFactory实现
				autowiringValue = AutowireUtils.resolveAutowiringValue(autowiringValue, requiredType);
				if (requiredType.isInstance(autowiringValue)) {
					return beanName;
				}
			}
		}

		return null;
	}

	public List<String> findAutowireCandidates(String beanName, Class<?> type, DependencyDescriptor descriptor) {
		/**
		 * 从BeanFactory中查找指定类型的Bean，返回所有符合Bean的BeanName
		 */
		String[] candidateNames = getBeanNamesForType(type, true, true);
		List<String> result = new ArrayList<>();

		for (String candidate : candidateNames) {
			if (!beanName.equals(candidate) && getBeanDefinition(candidate).isAutowireCandidate()) { // 排除自身
				result.add(candidate);
			}
		}

		if (!result.isEmpty()) {
			return result;
		}

		// 已注册的BeanDefinition中未找到符合的，那么寻找已生成的Bean。
		Set<Entry<String, Object>> entrySet = this.singletonObjects.entrySet();
		Class<?> clz = null;
		for (Entry<String, Object> entry : entrySet) {
			clz = entry.getValue().getClass();
			if (type.isAssignableFrom(clz)) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
		List<String> result = new ArrayList<>();
		for (String beanName : this.beanDefinitionNames) {
			BeanDefinition beanDefinition = getBeanDefinition(beanName);
			if (!beanDefinition.isAbstract() && findAnnotationOnBean(beanName, annotationType) != null) {
				result.add(beanName);
			}
		}
		return StringUtils.toStringArray(result);
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
			throws BeansException {
		String[] beanNames = getBeanNamesForAnnotation(annotationType);
		Map<String, Object> result = new LinkedHashMap<>(beanNames.length);
		for (String beanName : beanNames) {
			Object beanInstance = getBean(beanName);
			if (!(beanInstance instanceof NullBean)) {
				result.put(beanName, beanInstance);
			}
		}
		return result;
	}

	@Override
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException {
		RootBeanDefinition beanDefinition = this.getBeanDefinition(beanName);
		Class<?> beanClass = beanDefinition.getBeanClass();
		return beanClass.getDeclaredAnnotation(annotationType);
	}

	public String getSerializationId() {
		return serializationId;
	}

	public void setSerializationId(String serializationId) {
		this.serializationId = serializationId;
	}

	public Comparator<Object> getDependencyComparator() {
		return dependencyComparator;
	}

	public void setDependencyComparator(Comparator<Object> dependencyComparator) {
		this.dependencyComparator = dependencyComparator;
	}

	public String[] getFrozenBeanDefinitionNames() {
		return frozenBeanDefinitionNames;
	}

	public void setFrozenBeanDefinitionNames(String[] frozenBeanDefinitionNames) {
		this.frozenBeanDefinitionNames = frozenBeanDefinitionNames;
	}

	public void setConfigurationFrozen(boolean configurationFrozen) {
		this.configurationFrozen = configurationFrozen;
	}

	@Override
	public Map<String, BeanDefinition> getBeanDefinitions() throws BeansException {
		return this.beanDefinitionMap;
	}

	/**
	 * FacotryProvider及其子类依赖注入Bean封装类
	 * @author Azurite-Y
	 *
	 */
	@SuppressWarnings("serial")
	private class DependencyObjectProvider implements BeanObjectProvider<Object> {

		private final DependencyDescriptor descriptor;

		private final String beanName;

		public DependencyObjectProvider(DependencyDescriptor descriptor, String beanName) {
			this.descriptor = descriptor;
			this.beanName = beanName;
		}

		@Override
		public Object getObject() throws BeansException {
			Object result = doResolveDependency(this.descriptor, this.beanName, null);
			if (result == null) {
				throw new NoSuchBeanDefinitionException(this.descriptor.getSourceClass().getName());
			}
			return result;
		}

		@Override
		public Object getObject(final Object... args) throws BeansException {
			DependencyDescriptor descriptorToUse = new DependencyDescriptor(descriptor);
			Object result = doResolveDependency(descriptorToUse, this.beanName, null);
			if (result == null) {
				throw new NoSuchBeanDefinitionException(this.descriptor.getSourceClass().getName());
			}
			return result;
		}

		@Override
		public Object getIfAvailable() throws BeansException {
			DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
				@Override
				public boolean isRequired() {
					return false;
				}
			};
			return doResolveDependency(descriptorToUse, this.beanName, null);
		}

		@Override
		public Object getIfUnique() throws BeansException {
			DependencyDescriptor descriptorToUse = new DependencyDescriptor(this.descriptor) {
				@Override
				public boolean isRequired() {
					return false;
				}
			};
			return doResolveDependency(descriptorToUse, this.beanName, null);
		}

		@Override
		public Stream<Object> stream() {
			return resolveStream(false);
		}

		@Override
		public Stream<Object> orderedStream() {
			return resolveStream(true);
		}

		@SuppressWarnings("unchecked")
		private Stream<Object> resolveStream(boolean ordered) {
			DependencyDescriptor descriptorToUse = new StreamDependencyDescriptor(this.descriptor, ordered);
			Object result = doResolveDependency(descriptorToUse, this.beanName, null);
			return (result instanceof Stream ? (Stream<Object>) result : Stream.of(result));
		}
	}

	@SuppressWarnings({"serial", "unused"})
	private static class StreamDependencyDescriptor extends DependencyDescriptor {

		private final boolean ordered;

		public StreamDependencyDescriptor(DependencyDescriptor original, boolean ordered) {
			super(original);
			this.ordered = ordered;
		}

		public boolean isOrdered() {
			return this.ordered;
		}
	}
}

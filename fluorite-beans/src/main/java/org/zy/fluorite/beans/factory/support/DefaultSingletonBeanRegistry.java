package org.zy.fluorite.beans.factory.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.zy.fluorite.beans.factory.exception.BeanCreationException;
import org.zy.fluorite.beans.factory.exception.BeanCreationNotAllowedException;
import org.zy.fluorite.beans.factory.exception.BeanCurrentlyInCreationException;
import org.zy.fluorite.core.interfaces.SingletonBeanRegistry;
import org.zy.fluorite.core.interfaces.function.ObjectFactory;
import org.zy.fluorite.core.interfaces.instantiation.DisposableBean;
import org.zy.fluorite.core.subject.SimpleAliasRegistry;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月8日 下午2:41:24;
 * @Description
 */
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {

	/** 要保留的抑制异常的最大数目 */
	protected static final int SUPPRESSED_EXCEPTIONS_LIMIT = 100;

	/** 抑制异常的集合，可用于关联相关原因 */
	protected Set<Exception> suppressedExceptions;

	/** 存储单例对象集合，beanName : bean实例 */
	protected final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

	/** 存储单例对象的增强引用(getEarlyBeanReference()方法)集合 */
	protected final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

	/** 存储早期经过增强之后的单例对象集合 */
	protected final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

	/** 存储已注册的单例对象beanName，按注册顺序排列 */
	protected final Set<String> registeredSingletons = new LinkedHashSet<>(256);

	/** 存储当前正在被创建或初始化的beanName */
	protected final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

	/** 存储当前在创建检查中排除的bean名称 */
	protected final Set<String> inCreationCheckExclusions = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

	/** 判断当前是否正在销毁单例对象的标识 */
	protected boolean singletonsCurrentlyInDestruction = false;

	/** 一次性实例的bean名称 ：一次性bean实例 */
	protected final Map<String, Object> disposableBeans = new LinkedHashMap<>();

	/** 包含bean名称之间的映射：bean名称到bean包含的一组bean名称 */
	protected final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<>(16);

	/** 被依赖bean名称集合，被依赖beanName : 依赖beanName 【A有一属性B，此集合数据映射为 { [b:a] }】 */
	protected final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

	/** 依赖bean名称集合，依赖beanName : 被依赖beanName 【A有一属性B，此集合数据映射为 { [a:b] }】 */
	protected final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);

	public DefaultSingletonBeanRegistry() {
		super();
	}

	@Override
	public void registerSingleton(String beanName, Object singletonObject) {
		Assert.notNull(beanName, "Bean名称不能为null");
		Assert.notNull(singletonObject, "单例Bean对象不能为null");
		synchronized (this.singletonObjects) {
			Object oldObject = this.singletonObjects.get(beanName);
			if (oldObject != null) {
				throw new IllegalStateException("不能注册的单例对象 [" + singletonObject + "] 因为它已经注册了其他单例对象 [" + oldObject
						+ "] ，by beanName：" + beanName);
			}
			addSingleton(beanName, singletonObject);
		}
	}

	@Override
	public Object getSingleton(String beanName) {
		return getSingletonForObject(beanName, true);
	}

	@Override
	public boolean containsSingleton(String beanName) {
		return this.singletonObjects.containsKey(beanName);
	}

	@Override
	public String[] getSingletonNames() {
		synchronized (this.singletonObjects) {
			return StringUtils.toStringArray(this.registeredSingletons);
		}
	}

	@Override
	public int getSingletonCount() {
		synchronized (this.singletonObjects) {
			return this.registeredSingletons.size();
		}
	}

	@Override
	public Object getSingletonMutex() {
		return this.singletonObjects;
	}

	/**
	 * 改变指定Bean是否正在被创建或初始化的状态标识
	 * 
	 * @param beanName
	 * @param inCreation
	 */
	public void setCurrentlyInCreation(String beanName, boolean inCreation) {
		Assert.notNull(beanName, "Bean name must not be null");
		if (!inCreation) {
			this.inCreationCheckExclusions.add(beanName);
		} else {
			this.inCreationCheckExclusions.remove(beanName);
		}
	}

	/**
	 * 单例对象注册
	 * 
	 * @param beanName
	 * @param singletonObject
	 */
	protected void addSingleton(String beanName, Object singletonObject) {
		synchronized (this.singletonObjects) {
			this.singletonObjects.put(beanName, singletonObject);
			this.singletonFactories.remove(beanName);
			this.earlySingletonObjects.remove(beanName);
			this.registeredSingletons.add(beanName);
		}
	}

	/**
	 * 单例对象的增强型引用注册
	 * 
	 * @param beanName
	 * @param singletonFactory
	 */
	protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(singletonFactory, "此单例的增强型引用singletonFactory不能为null");
		synchronized (this.singletonObjects) {
			if (!this.singletonObjects.containsKey(beanName)) {
				this.singletonFactories.put(beanName, singletonFactory);
				this.earlySingletonObjects.remove(beanName);
				this.registeredSingletons.add(beanName);
			}
		}
	}

	/**
	 * 获得单例对象，且显式的指定是否进行增强型引用
	 * 
	 * @param beanName
	 * @param singletonFactory
	 */
	protected Object getSingletonForObject(String beanName, boolean allowEarlyReference) {
		Object singletonObject = this.singletonObjects.get(beanName);
		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
			synchronized (this.singletonObjects) {
				singletonObject = this.earlySingletonObjects.get(beanName);
				if (singletonObject == null && allowEarlyReference) {
					ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
					if (singletonFactory != null) {
						/**
						 * 执行之前存储的lambda表达式。可能执行AOP调用
						 * 实际调用的是AbstractAutowireCapableBeanFactory.getEarlyBeanReference(String,
						 * RootBeanDefinition, Object)方法， 返回的是实例化过的Bean对象
						 */
						singletonObject = singletonFactory.getObject();
						this.earlySingletonObjects.put(beanName, singletonObject);
						this.singletonFactories.remove(beanName);
					}
				}
			}
		}
		return singletonObject;
	}

	public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(beanName, " Bean名称不能为空");
		synchronized (this.singletonObjects) {
			// 从单例对象缓存集合中尝试获取，有结果则之间返回
			Object singletonObject = this.singletonObjects.get(beanName);
			if (singletonObject == null) {
				if (this.singletonsCurrentlyInDestruction) { // 指示我们当前是否在DestroySingleton中的标志。
					throw new BeanCreationNotAllowedException(
							" 此工厂的单例处于销毁状态时不允许创建单例bean (不要在销毁方法实现中从BeanFactory实现中获得bean!)，by bean：" + beanName);
				}
				if (DebugUtils.debug) {
					logger.info("开始创建单例Bean，by：" + beanName);
				}
				/**
				 * 在单例创建之前回调。默认实现将单例注册为当前正在创建的
				 * 若正在创建的单例bean集合已设置当前beanName，则抛出BeanCurrentlyInCreationException异常
				 */
				beforeSingletonCreation(beanName);
				boolean newSingleton = false;
				// suppressedExceptions(默认：null) - 抑制异常列表，可用于关联相关原因
				boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
				if (recordSuppressedExceptions) {
					this.suppressedExceptions = new LinkedHashSet<>();
				}
				try {
					// 此处调用的是上级调用方法doGetBean中定义的Lambda表达式中的方法内容，相当于回调createBean()方法
					singletonObject = singletonFactory.getObject();
					newSingleton = true;
				} catch (IllegalStateException ex) {
					// 同时是否隐式地出现了singleton对象 -> 如果是，请继续，因为异常指示该状态
					singletonObject = this.singletonObjects.get(beanName);
					if (singletonObject == null) {
						throw ex;
					}
				} catch (BeanCreationException ex) {
					if (recordSuppressedExceptions) {
						for (Exception suppressedException : this.suppressedExceptions) {
							ex.addRelatedCause(suppressedException);
						}
					}
					throw ex;
				} finally {
					if (recordSuppressedExceptions) {
						this.suppressedExceptions = null;
					}
					/**
					 * 创建单例后回调。默认实现将单例标记为不再处于创建中
					 * 若正在创建单例集合中未有当前beanName则触发异常IllegalStateException(“单例 %beanName% 未正在创建中”)
					 */
					afterSingletonCreation(beanName);
				}
				if (newSingleton) {
					// 将给定的单例对象添加到此工厂的单例缓存
					addSingleton(beanName, singletonObject);
				}
			}
			return singletonObject;
		}
	}

	/**
	 * 判断指定bean是否正在被创建或初始化
	 * 
	 * @param beanName
	 * @return
	 */
	public boolean isSingletonCurrentlyInCreation(String beanName) {
		return this.singletonsCurrentlyInCreation.contains(beanName);
	}

	/**
	 * 判断指定bean是否正在被创建或初始化
	 * 
	 * @param beanName
	 * @return
	 */
	public boolean isCurrentlyInCreation(String beanName) {
		Assert.notNull(beanName, "beanName不能为null");
		return (!this.inCreationCheckExclusions.contains(beanName) && isSingletonCurrentlyInCreation(beanName));
	}

	public void registerDependentBean(String beanName, String dependentBeanName) {
		String canonicalName = canonicalName(beanName);

		synchronized (this.dependentBeanMap) {
			Set<String> dependentBeans = this.dependentBeanMap.computeIfAbsent(canonicalName,
					k -> new LinkedHashSet<>(8));
			if (!dependentBeans.add(dependentBeanName)) {
				return;
			}
		}

		synchronized (this.dependenciesForBeanMap) {
			Set<String> dependenciesForBean = this.dependenciesForBeanMap.computeIfAbsent(dependentBeanName,
					k -> new LinkedHashSet<>(8));
			dependenciesForBean.add(canonicalName);
		}

		if (DebugUtils.debug) {
			logger.info("将'{}'注册为'{}'的依赖项。", beanName, dependentBeanName);
		}
	}

	/**
	 * 判断dependentBeanName是否是beanName的依赖项
	 * 
	 * @param beanName
	 * @param dependentBeanName
	 * @return
	 */
	protected boolean isDependent(String beanName, String dependentBeanName) {
		synchronized (this.dependentBeanMap) {
			return isDependent(beanName, dependentBeanName, null);
		}
	}

	/**
	 * 依赖项判断。同时避免循环依赖
	 * 
	 * @param beanName
	 * @param dependentBeanName
	 * @param alreadySeen
	 * @return
	 */
	private boolean isDependent(String beanName, String dependentBeanName, Set<String> alreadySeen) {
		if (alreadySeen != null && alreadySeen.contains(beanName)) {
			return false;
		}
		String canonicalName = canonicalName(beanName);
		Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
		if (dependentBeans == null) {
			return false;
		}
		if (dependentBeans.contains(dependentBeanName)) {
			return true;
		}
		for (String transitiveDependency : dependentBeans) {
			if (alreadySeen == null) {
				alreadySeen = new HashSet<>();
			}
			alreadySeen.add(beanName);
			// 递归检查依赖Bean的依赖关系，防止A依赖B，B依赖C，C又依赖A，现在是从C开始递归检查
			if (isDependent(transitiveDependency, dependentBeanName, alreadySeen)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 销毁全部标记为一次性的Bean
	 */
	public void destroySingletons() {
		logger.info("开始销毁单例，by：" + this);

		synchronized (this.singletonObjects) {
			this.singletonsCurrentlyInDestruction = true;
		}

		String[] disposableBeanNames;
		synchronized (this.disposableBeans) {
			disposableBeanNames = StringUtils.toStringArray(this.disposableBeans.keySet());
		}
		for (String disposableBeanName : disposableBeanNames) {
			destroySingleton(disposableBeanName);
		}

		this.containedBeanMap.clear();
		this.dependentBeanMap.clear();
		this.dependenciesForBeanMap.clear();

		clearSingletonCache();
		logger.info("销毁单例完成...");
	}

	/**
	 * 清空容器缓存
	 */
	protected void clearSingletonCache() {
		synchronized (this.singletonObjects) {
			this.singletonObjects.clear();
			this.singletonFactories.clear();
			this.earlySingletonObjects.clear();
			this.registeredSingletons.clear();
			this.singletonsCurrentlyInDestruction = false;
		}
	}

	protected void removeSingleton(String beanName) {
		synchronized (this.singletonObjects) {
			this.singletonObjects.remove(beanName);
			this.singletonFactories.remove(beanName);
			this.earlySingletonObjects.remove(beanName);
			this.registeredSingletons.remove(beanName);
		}
	}

	/**
	 * 销毁指定bean
	 */
	public void destroySingleton(String beanName) {
		removeSingleton(beanName);

		// 销毁相应的DisposableBean实例。
		DisposableBean disposableBean;
		synchronized (this.disposableBeans) {
			disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
		}
		destroyBean(beanName, disposableBean);
	}

	protected void destroyBean(String beanName, DisposableBean bean) {
		Set<String> dependencies;
		synchronized (this.dependentBeanMap) {
			// 清空依赖关系，返回依赖其的beanName
			dependencies = this.dependentBeanMap.remove(beanName);
		}
		if (dependencies != null) {
			for (String dependentBeanName : dependencies) {
				destroySingleton(dependentBeanName);
			}
		}

		if (bean != null) {
			try {
				// 调用DisposableBean实现的destroy()方法
				bean.destroy();
			} catch (Throwable ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("销毁bean出现异常，by bean： '" + beanName, ex);
				}
			}
		}

		// 从被依赖Bean集合中删除
		synchronized (this.dependentBeanMap) {
			for (Iterator<Map.Entry<String, Set<String>>> it = this.dependentBeanMap.entrySet().iterator(); it
					.hasNext();) {
				Map.Entry<String, Set<String>> entry = it.next();
				Set<String> dependenciesToClean = entry.getValue();
				dependenciesToClean.remove(beanName);
				if (dependenciesToClean.isEmpty()) {
					it.remove();
				}
			}
		}

		// 移除已销毁bean的依赖项信息
		this.dependenciesForBeanMap.remove(beanName);
	}

	/**
	 * 注册在创建单例bean实例期间发生的被抑制的异常，例如临时循环引用解析问题。
	 * 
	 * @param ex
	 */
	protected void onSuppressedException(Exception ex) {
		synchronized (this.singletonObjects) {
			if (this.suppressedExceptions != null && this.suppressedExceptions.size() < SUPPRESSED_EXCEPTIONS_LIMIT) {
				this.suppressedExceptions.add(ex);
			}
		}
	}

	/**
	 * 将当前Bean标记为正在创建，若已标记为正在创建则抛出异常
	 * 
	 * @param beanName
	 */
	protected void beforeSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.add(beanName)) {
			throw new BeanCurrentlyInCreationException(beanName);
		}
	}

	/**
	 * 将当前Bean为正在创建的标记移除，若没有标记则抛出异常
	 */
	protected void afterSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.contains(beanName)
				&& !this.singletonsCurrentlyInCreation.remove(beanName)) {
			throw new IllegalStateException("单例对象未正在创建，by beanName：" + beanName);
		}
	}

	/**
	 * 注册可销毁的一次性bean
	 * 
	 * @param beanName
	 * @param bean
	 */
	public void registerDisposableBean(String beanName, DisposableBean bean) {
		synchronized (this.disposableBeans) {
			this.disposableBeans.put(beanName, bean);
		}
	}
}

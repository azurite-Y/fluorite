package org.zy.fluorite.context.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.aop.utils.AopProxyUtils;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.aware.BeanClassLoaderAware;
import org.zy.fluorite.beans.factory.aware.BeanFactoryAware;
import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableBeanFactory;
import org.zy.fluorite.beans.support.AnnotationAwareOrderComparator;
import org.zy.fluorite.context.event.interfaces.ApplicationEventMulticaster;
import org.zy.fluorite.context.event.interfaces.ApplicationListener;
import org.zy.fluorite.context.event.interfaces.GenericApplicationListener;
import org.zy.fluorite.context.event.interfaces.SmartApplicationListener;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;

/**
 * @DateTime 2020年6月18日 下午12:50:06;
 * @author zy(azurite-Y);
 * @Description ApplicationEventMulticaster接口的抽象实现，提供基本的侦听器注册功能。
 */
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster, BeanClassLoaderAware, BeanFactoryAware {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private final ListenerRetriever defaultRetriever = new ListenerRetriever(false);

	final Map<ListenerCacheKey, ListenerRetriever> retrieverCache = new ConcurrentHashMap<>(64);

	private ClassLoader beanClassLoader;

	private ConfigurableBeanFactory beanFactory;

	private Object retrievalMutex = this.defaultRetriever;
	
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		Assert.isTrue(beanFactory instanceof ConfigurableBeanFactory , "‘beanFactory'未实现ConfigurableBeanFactory接口");
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
		if (this.beanClassLoader == null) {
			this.beanClassLoader = this.beanFactory.getBeanClassLoader();
		}
		this.retrievalMutex = this.beanFactory.getSingletonMutex();
	}
	
	private ConfigurableBeanFactory getBeanFactory() {
		Assert.notNull(this.beanFactory,"ApplicationEventMulticaster无法获得侦听器Bean，因为BeanFactory实现为null");
		return this.beanFactory;
	}
	
	@Override
	public void addApplicationListener(ApplicationListener<?> listener) {
		synchronized (this.retrievalMutex) {
			Object singletonTarget = AopProxyUtils.getSingletonTarget(listener);
			if (singletonTarget instanceof ApplicationListener) {
				this.defaultRetriever.applicationListeners.remove(singletonTarget);
			}
			this.defaultRetriever.applicationListeners.add(listener);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void addApplicationListenerBean(String listenerBeanName) {
		synchronized (this.retrievalMutex) {
			this.defaultRetriever.applicationListenerBeans.add(listenerBeanName);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void removeApplicationListener(ApplicationListener<?> listener) {
		synchronized (this.retrievalMutex) {
			this.defaultRetriever.applicationListeners.remove(listener);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void removeApplicationListenerBean(String listenerBeanName) {
		synchronized (this.retrievalMutex) {
			this.defaultRetriever.applicationListenerBeans.remove(listenerBeanName);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void removeAllListeners() {
		synchronized (this.retrievalMutex) {
			this.defaultRetriever.applicationListeners.clear();
			this.defaultRetriever.applicationListenerBeans.clear();
			this.retrieverCache.clear();
		}
	}

	/**
	 * 返回包含所有ApplicationListeners的集合
	 */
	protected Collection<ApplicationListener<?>> getApplicationListeners() {
		synchronized (this.retrievalMutex) {
			return this.defaultRetriever.getApplicationListeners();
		}
	}
	
	protected Collection<ApplicationListener<?>> getApplicationListeners(ApplicationEvent event, ResolvableType eventType) {
		// 获得最初触发事件的对象
		Object source = event.getSource();
		Class<?> sourceType = (source != null ? source.getClass() : null);
		ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);

		ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
		if (retriever != null) {
			return retriever.getApplicationListeners();
		}

		if (this.beanClassLoader == null ||
				(ClassUtils.isCacheSafe(event.getClass(), this.beanClassLoader) && (sourceType == null 
					|| ClassUtils.isCacheSafe(sourceType, this.beanClassLoader)))) {
			synchronized (this.retrievalMutex) {
				retriever = this.retrieverCache.get(cacheKey);
				if (retriever != null) {
					return retriever.getApplicationListeners();
				}
				retriever = new ListenerRetriever(true);
				Collection<ApplicationListener<?>> listeners =	retrieveApplicationListeners(eventType, sourceType, retriever);
				this.retrieverCache.put(cacheKey, retriever);
				return listeners;
			}
		} else {
			// 无ListenerRetriever缓存->无需同步
			return retrieveApplicationListeners(eventType, sourceType, null);
		}
	}

	/**
	 * 实际检索给定事件和源类型的应用程序侦听器
	 * @param eventType - 事件类型
	 * @param sourceType - 事件源类型
	 * @param retriever - 需要填充的ListenerRetriever
	 * @return 给定事件和源类型的应用程序侦听器的预筛选列表
	 */
	private Collection<ApplicationListener<?>> retrieveApplicationListeners(ResolvableType eventType, Class<?> sourceType, ListenerRetriever retriever) {
		List<ApplicationListener<?>> allListeners = new ArrayList<>();
		Set<ApplicationListener<?>> listeners;
		Set<String> listenerBeans;
		synchronized (this.retrievalMutex) {
			listeners = new LinkedHashSet<>(this.defaultRetriever.applicationListeners);
			listenerBeans = new LinkedHashSet<>(this.defaultRetriever.applicationListenerBeans);
		}

		// 添加以编程方式注册的侦听器，包括来自ApplicationListenerDetector的侦听器（单例bean和内部bean）
		for (ApplicationListener<?> listener : listeners) {
			if (supportsEvent(listener, eventType, sourceType)) {
				if (retriever != null) {
					retriever.applicationListeners.add(listener);
				}
				allListeners.add(listener);
			}
		}

		// 按bean名称添加监听器，可能与上面以编程方式注册的监听器重叠，但这里可能有其他元数据。
		if (!listenerBeans.isEmpty()) {
			ConfigurableBeanFactory beanFactory = getBeanFactory();
			for (String listenerBeanName : listenerBeans) {
				try {
					if (supportsEvent(beanFactory, listenerBeanName, eventType)) {
						ApplicationListener<?> listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
						if (!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType)) {
							if (retriever != null) {
								if (beanFactory.isSingleton(listenerBeanName)) {
									retriever.applicationListeners.add(listener);
								} else {
									retriever.applicationListenerBeans.add(listenerBeanName);
								}
							}
							allListeners.add(listener);
						}
					} else {
						// 删除最初来自ApplicationListenerDetector的不匹配侦听器，可能由上面的其他BeanDefinition元数据（例如工厂方法泛型）排除。
						Object listener = beanFactory.getSingleton(listenerBeanName);
						if (retriever != null) {
							retriever.applicationListeners.remove(listener);
						}
						allListeners.remove(listener);
					}
				} catch (NoSuchBeanDefinitionException ex) {
					// 单例侦听器实例（不支持bean定义）销毁或未找到
				}
			}
		}

		AnnotationAwareOrderComparator.sort(allListeners);
		if (retriever != null && retriever.applicationListenerBeans.isEmpty()) {
			retriever.applicationListeners.clear();
			retriever.applicationListeners.addAll(allListeners);
		}
		return allListeners;
	}

	/**
	 * 通过检查其泛型声明的事件类型及早对其进行筛选。
	 * @param beanFactory
	 * @param listenerBeanName
	 * @param eventType - 要检查的事件类型
	 * @return 给定的侦听器是否应包含在给定事件类型的候选项
	 */
	private boolean supportsEvent(ConfigurableBeanFactory beanFactory, String listenerBeanName, ResolvableType eventType) {
		Class<?> listenerType = beanFactory.getType(listenerBeanName);
		if (listenerType == null || GenericApplicationListener.class.isAssignableFrom(listenerType) ||
				SmartApplicationListener.class.isAssignableFrom(listenerType)) {
			return true;
		}
		if (!supportsEvent(listenerType, eventType)) {
			return false;
		}
		
		try {
			RootBeanDefinition bd = beanFactory.getBeanDefinition(listenerBeanName);
			
			// 获得bd所指代类的泛型信息
			ResolvableType genericEventType = bd.getResolvableType().as(ApplicationListener.class).getGeneric();
			return (genericEventType == ResolvableType.NONE || genericEventType.isAssignableFrom(eventType));
		}
		catch (NoSuchBeanDefinitionException ex) {
			// 忽略-不需要检查手动注册的单例的可解析类型
			return true;
		}
	}

	/**
	 * 过检查其泛型声明的事件类型及早对其进行筛选。
	 */
	protected boolean supportsEvent(Class<?> listenerType, ResolvableType eventType) {
		ResolvableType declaredEventType = GenericApplicationListenerAdapter.resolveDeclaredEventType(listenerType);
		return (declaredEventType == null || declaredEventType.isAssignableFrom(eventType));
	}

	/**
	 * 确定给定的侦听器是否支持给定的事件
	 */
	protected boolean supportsEvent(	ApplicationListener<?> listener, ResolvableType eventType, Class<?> sourceType) {
		GenericApplicationListener smartListener = (listener instanceof GenericApplicationListener ?
				(GenericApplicationListener) listener : new GenericApplicationListenerAdapter(listener));
		return (smartListener.supportsEventType(eventType) && smartListener.supportsSourceType(sourceType));
	}
	
	private static final class ListenerCacheKey implements Comparable<ListenerCacheKey> {

		private final ResolvableType eventType;

		private final Class<?> sourceType;

		public ListenerCacheKey(ResolvableType eventType, Class<?> sourceType) {
			Assert.notNull(eventType, "事件类型不能为null");
			this.eventType = eventType;
			this.sourceType = sourceType;
		}

		
		/**
		 * 首先通过事件类型比对，然后根据触发事件的源类型比对
		 */
		@Override
		public int compareTo(ListenerCacheKey other) {
			/**
			 * 如果参数字符串等于此字符串，则值为0；如果此字符串在词汇上小于字符串参数，则值小于0；
			 * 如果此字符串在词汇上大于字符串参数，则值a value大于0。
			 */
			int result = this.eventType.toString().compareTo(other.eventType.toString());
			if (result == 0) {
				if (this.sourceType == null) {
					return (other.sourceType == null ? 0 : -1);
				}
				if (other.sourceType == null) {
					return 1;
				}
				result = this.sourceType.getName().compareTo(other.sourceType.getName());
			}
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ListenerCacheKey other = (ListenerCacheKey) obj;
			if (eventType == null) {
				if (other.eventType != null)
					return false;
			} else if (!eventType.equals(other.eventType))
				return false;
			if (sourceType == null) {
				if (other.sourceType != null)
					return false;
			} else if (!sourceType.equals(other.sourceType))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
			result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
			return result;
		}

		@Override
		public String toString() {
			return "ListenerCacheKey [eventType=" + eventType + ", sourceType=" + sourceType + "]";
		}
	}
	
	private class ListenerRetriever {

		public final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();

		public final Set<String> applicationListenerBeans = new LinkedHashSet<>();
		
		/** 是否需要预过滤 */
		private final boolean preFiltered;

		public ListenerRetriever(boolean preFiltered) {
			this.preFiltered = preFiltered;
		}

		public Collection<ApplicationListener<?>> getApplicationListeners() {
			List<ApplicationListener<?>> allListeners = new ArrayList<>(this.applicationListeners.size() + this.applicationListenerBeans.size());
			allListeners.addAll(this.applicationListeners);
			if (!this.applicationListenerBeans.isEmpty()) {
				BeanFactory beanFactory = getBeanFactory();
				for (String listenerBeanName : this.applicationListenerBeans) {
					try {
						ApplicationListener<?> listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
						if (this.preFiltered || !allListeners.contains(listener)) {
							allListeners.add(listener);
						}
					}
					catch (NoSuchBeanDefinitionException ex) {}
				}
			}
			if (!this.preFiltered || !this.applicationListenerBeans.isEmpty()) {
				AnnotationAwareOrderComparator.sort(allListeners);
			}
			return allListeners;
		}
	}
}

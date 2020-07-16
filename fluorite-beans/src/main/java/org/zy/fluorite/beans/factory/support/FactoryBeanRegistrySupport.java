package org.zy.fluorite.beans.factory.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.zy.fluorite.beans.factory.exception.BeanCreationException;
import org.zy.fluorite.core.interfaces.instantiation.FactoryBean;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月8日 下午3:43:04;
 * @Description 缓存FactoryBean实例及其方法调用处理
 */
public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {

	/** FactoryBean实例缓存 */
	protected final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>(16);

	public FactoryBeanRegistrySupport() {
	}

	/**
	 * 尝试从factoryBeanObjectCache容器中获得初始化此Bean的FactoryBean实现
	 * 
	 * @param beanName
	 * @return
	 */
	protected Object getCachedObjectForFactoryBean(String beanName) {
		return this.factoryBeanObjectCache.get(beanName);
	}

	/**
	 * 为FactoryBean创建的实例执行的后处理逻辑
	 * 
	 * @param object
	 * @param beanName
	 * @return
	 */
	protected abstract Object postProcessObjectFromFactoryBean(Object object, String beanName);

	@Override
	protected void clearSingletonCache() {
		synchronized (getSingletonMutex()) {
			super.clearSingletonCache();
			this.factoryBeanObjectCache.clear();
		}
	}

	/**
	 * 通过FactoryBean实例调用getObject()方法创建Bean
	 * 
	 * @param factory
	 * @param beanName
	 * @return
	 */
	protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName) {
		Object object = null;
		if (factory.isSingleton() && containsSingleton(beanName)) {
			synchronized (getSingletonMutex()) {
				Object factoryBean = this.factoryBeanObjectCache.get(beanName);
				if (factoryBean == null) {
					try {
						DebugUtils.log(logger, "需要调用FactoryBean实现的getObject方法进行实例化，by："+factory);
						object = factory.getObject();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
					if (alreadyThere != null) {
						factoryBean = alreadyThere;
					} else {
						if (isSingletonCurrentlyInCreation(beanName)) {
							return object;
						}
						beforeSingletonCreation(beanName);
						try {
							// 为此实例应用初始化之后的后处理器
							object = postProcessObjectFromFactoryBean(object, beanName);
						} catch (Throwable ex) {
							throw new BeanCreationException("为此FactoryBean实现执行后处理器程中出现异常，by " + beanName, ex);
						} finally {
							super.afterSingletonCreation(beanName);
						}
					}
					
					if (containsSingleton(beanName)) {
						// 替换原来的FactoryBean实例，而由FactoryBean创建的实例替代
						this.factoryBeanObjectCache.put(beanName, object);
						DebugUtils.log(logger, "替换原来的FactoryBean实例，而由FactoryBean创建的实例替代，by："+beanName+"，创建的实例："+object);
					}
				}
			}
		} else {
			try {
				object = factory.getObject();
				object = postProcessObjectFromFactoryBean(object, beanName);
			} catch (Throwable ex) {
				throw new BeanCreationException("为此FactoryBean实现执行后处理器程中出现异常，by " + beanName, ex);
			}
		}
		return object;
	}
}

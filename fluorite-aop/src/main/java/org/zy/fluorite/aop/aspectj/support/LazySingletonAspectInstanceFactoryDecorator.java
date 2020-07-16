package org.zy.fluorite.aop.aspectj.support;

import java.io.Serializable;

import org.zy.fluorite.aop.aspectj.interfaces.MetadataAwareAspectInstanceFactory;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月5日 下午10:15:14;
 * @author zy(azurite-Y);
 * @Description 此类使MetadataAwareAspectInstanceFactory实现仅实例化一次
 */
@SuppressWarnings("serial")
public class LazySingletonAspectInstanceFactoryDecorator implements MetadataAwareAspectInstanceFactory, Serializable {
	private final MetadataAwareAspectInstanceFactory aspectInstanceFactory;

	private Object mutex;

	
	public LazySingletonAspectInstanceFactoryDecorator(MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
		Assert.notNull(aspectInstanceFactory, "aspectInstanceFactory不能为null");
		this.aspectInstanceFactory = aspectInstanceFactory;
	}

	@Override
	public Object getAspectInstance() {
		// 首先获得本地锁，单例切面为null，原型切面则为BeanFactory持有的单例Bean实例集合(singletonObjects)
		if (this.mutex == null) {
			this.mutex = getAspectCreationMutex();
			if (mutex == null) {
				mutex = aspectInstanceFactory.getAspectInstance();
			} else {
				synchronized (mutex) { 	// 对单例对象结合加锁再获取当前切面Bean
					if (mutex == null) {
						mutex = aspectInstanceFactory.getAspectInstance();
					}
				}
			}
		}
		return mutex;
	}

	public boolean isMaterialized() {
		return (this.mutex != null);
	}

	@Override
	public ClassLoader getAspectClassLoader() {
		return this.aspectInstanceFactory.getAspectClassLoader();
	}

	@Override
	public int getOrder() {
		return aspectInstanceFactory.getOrder();
	}

	@Override
	public AnnotationMetadata getAspectMetadata() {
		return aspectInstanceFactory.getAspectMetadata();
	}

	@Override
	public Object getAspectCreationMutex() {
		return aspectInstanceFactory.getAspectCreationMutex();
	}

	@Override
	public String getName() {
		return this.aspectInstanceFactory.getName();
	}

	@Override
	public Class<?> getType() {
		return this.aspectInstanceFactory.getType();
	}

}

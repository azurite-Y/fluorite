package org.zy.fluorite.aop.aspectj.support;

import java.io.Serializable;

import org.zy.fluorite.aop.aspectj.interfaces.MetadataAwareAspectInstanceFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.core.annotation.Order;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月5日 下午10:14:31;
 * @author zy(azurite-Y);
 * @Description 单例的切面工厂类，若是原型则需使用{@linkplain LazySingletonAspectInstanceFactoryDecorator }
 */
@SuppressWarnings("serial")
public class BeanFactoryAspectInstanceFactory implements MetadataAwareAspectInstanceFactory, Serializable {
	private final ConfigurableListableBeanFactory beanFactory;

	private final String beanName;
	
	private final Class<?> beanType;

	private final AnnotationMetadata metadata;

	public BeanFactoryAspectInstanceFactory(ConfigurableListableBeanFactory beanFactory, String beanName,	AnnotationMetadata metadata, Class<?> beanType) {
		Assert.notNull(beanFactory, "beanFactory不能为null");
		Assert.notNull(beanName, "beanName不能为null或空串");
		Assert.notNull(metadata, "切面类的metadata不能为null");
		this.beanFactory = beanFactory;
		this.beanName = beanName;
		this.metadata = metadata;
		this.beanType = beanType;
	}

	@Override
	public Object getAspectInstance() {
		return this.beanFactory.getBean(beanName);
	}

	@Override
	public ClassLoader getAspectClassLoader() {
		return this.beanFactory.getBeanClassLoader();
	}

	@Override
	public int getOrder() {
		Class<?> type = this.beanFactory.getType(this.beanName);
		if (type != null) {
			if (Ordered.class.isAssignableFrom(type) && this.beanFactory.isSingleton(this.beanName)) {
				return ((Ordered) this.beanFactory.getBean(this.beanName)).getOrder();
			}
			Order order = metadata.getAnnotationForClass(Order.class);
			return (order != null ? order.value() : Ordered.LOWEST_PRECEDENCE);
		}
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public AnnotationMetadata getAspectMetadata() {
		return this.metadata;
	}

	@Override
	public Object getAspectCreationMutex() {
		if (this.beanFactory.isSingleton(this.beanName)) {
			// 单例对象不需要本地锁
			return null;
		} else if (this.beanFactory instanceof ConfigurableBeanFactory) {
			return ((ConfigurableBeanFactory) this.beanFactory).getSingletonMutex();
		} else {
			return this;
		}
	}

	@Override
	public String getName() {
		return this.beanName;
	}

	@Override
	public Class<?> getType() {
		return this.beanType;
	}
	
}

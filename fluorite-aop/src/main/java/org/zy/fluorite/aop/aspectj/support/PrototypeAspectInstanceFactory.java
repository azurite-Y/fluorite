package org.zy.fluorite.aop.aspectj.support;

import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月5日 下午10:31:04;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public class PrototypeAspectInstanceFactory extends BeanFactoryAspectInstanceFactory {
	public PrototypeAspectInstanceFactory(ConfigurableListableBeanFactory beanFactory , String name ,	AnnotationMetadata metadata, Class<?> beanType) {
		super(beanFactory, name, metadata, beanType);
		Assert.isTrue(beanFactory.isPrototype(name), "非原型的切面Bean不能使用PrototypeAspectInstanceFactory包装,");
	}

}

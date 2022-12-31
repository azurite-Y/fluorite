package org.zy.fluorite.autoconfigure.web.servlet.customizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.zy.fluorite.autoconfigure.web.server.interfaces.ErrorPageRegistrar;
import org.zy.fluorite.autoconfigure.web.server.interfaces.ErrorPageRegistry;
import org.zy.fluorite.beans.factory.aware.BeanFactoryAware;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ListableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanPostProcessor;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.beans.support.AnnotationAwareOrderComparator;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description 将所有来自bean工厂的 {@link ErrorPageRegistrar} 应用到ErrorPageRegistry bean的 {@link BeanPostProcessor} 
 */
public class ErrorPageRegistrarBeanPostProcessor  implements BeanPostProcessor, BeanFactoryAware {

	private ListableBeanFactory beanFactory;

	private List<ErrorPageRegistrar> registrars;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		Assert.isInstanceOf(ListableBeanFactory.class, beanFactory, "ErrorPageRegistrarBeanPostProcessor 只能与ListableBeanFactory一起使用");
		this.beanFactory = (ListableBeanFactory) beanFactory;
	}

	
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, BeanDefinition beanDefinition) throws BeansException {
		if (bean instanceof ErrorPageRegistry) {
			postProcessBeforeInitialization((ErrorPageRegistry) bean);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, BeanDefinition beanDefinition) throws BeansException {
		return bean;
	}

	private void postProcessBeforeInitialization(ErrorPageRegistry registry) {
		for (ErrorPageRegistrar registrar : getRegistrars()) {
			registrar.registerErrorPages(registry);
		}
	}

	private Collection<ErrorPageRegistrar> getRegistrars() {
		if (this.registrars == null) {
			// 查找不包括父上下文
			this.registrars = new ArrayList<>(this.beanFactory.getBeansOfType(ErrorPageRegistrar.class, false, false).values());
			this.registrars.sort(AnnotationAwareOrderComparator.INSTANCE);
			this.registrars = Collections.unmodifiableList(this.registrars);
		}
		return this.registrars;
	}

}
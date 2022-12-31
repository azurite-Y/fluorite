package org.zy.fluorite.context.support;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.aop.interfaces.TargetSource;
import org.zy.fluorite.aop.proxy.ProxyFactory;
import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.zy.fluorite.beans.factory.support.DependencyDescriptor;
import org.zy.fluorite.beans.interfaces.AutowireCandidateResolver;
import org.zy.fluorite.core.annotation.Lazy;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.subject.ExecutableParameter;
import org.zy.fluorite.core.utils.AnnotationUtils;
import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月14日 下午3:10:12;
 * @Description
 */
public class QualifierAnnotationAutowireCandidateResolver implements AutowireCandidateResolver {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected DefaultListableBeanFactory beanFactory;
	
	public QualifierAnnotationAutowireCandidateResolver(AbstractAutowireCapableBeanFactory beanFactory) {
		Assert.isTrue(beanFactory instanceof DefaultListableBeanFactory, "BeanFactory实例需要是DefaultListableBeanFactory");
		this.beanFactory = (DefaultListableBeanFactory) beanFactory;
	}

	@Override
	public Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName) {
		return (isLazy(descriptor) ? buildLazyResolutionProxy(descriptor, beanName) : null);
	}

	protected boolean isLazy(DependencyDescriptor descriptor) {
		Field field = descriptor.getField();
		if (field != null) {
			return foundLazy(field);
		} else {
			ExecutableParameter executableParameter = descriptor.getExecutableParameter();
			Executable executable = executableParameter.getExecutable();
			for (Parameter parameter : executable.getParameters()) {
				if (foundLazy(parameter)) {
					return true;
				}
			}
			return false;
		}
		
	}
	
	private boolean foundLazy(AnnotatedElement element) {
		AnnotationAttributes annotationAttributes = AnnotationUtils.getAnnotationAttributes(element);
		if (annotationAttributes == null) {return false;}
		Lazy lazy = annotationAttributes.getAnnotation(Lazy.class);
		if (lazy == null) {return false;}
		return lazy.value();
	}
	
	protected Object buildLazyResolutionProxy(final DependencyDescriptor descriptor, final String beanName) {
		TargetSource ts = new TargetSource() {
			@Override
			public Class<?> getTargetClass() {
				return descriptor.getResolveDependencyType();
			}

			@Override
			public boolean isStatic() {
				return false;
			}

			@Override
			public Object getTarget() {
				Object target = beanFactory.doResolveDependency(descriptor, beanName, null);
				if (target == null) {
					Class<?> type = getTargetClass();
					if (Map.class == type) {
						return Collections.emptyMap();
					} else if (List.class == type) {
						return Collections.emptyList();
					} else if (Set.class == type || Collection.class == type) {
						return Collections.emptySet();
					}
					throw new NoSuchBeanDefinitionException("延迟注入点不存在可选依赖项，by："+descriptor.getResolveDependencyType());
				}
				return target;
			}

			@Override
			public void releaseTarget(Object target) {}
		};
		ProxyFactory pf = new ProxyFactory();
		pf.setTargetSource(ts);
		Class<?> dependencyType = descriptor.getResolveDependencyType();
		if (dependencyType.isInterface()) {
			pf.addInterface(dependencyType);
		}
		Object proxy = pf.getProxy(beanFactory.getBeanClassLoader());
		logger.info("已创建的懒加载代理对象："+proxy.getClass());
		return proxy;
	}
}

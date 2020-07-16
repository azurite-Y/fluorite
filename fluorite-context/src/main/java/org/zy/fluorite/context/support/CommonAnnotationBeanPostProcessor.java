package org.zy.fluorite.context.support;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.zy.fluorite.aop.interfaces.TargetSource;
import org.zy.fluorite.aop.proxy.ProxyFactory;
import org.zy.fluorite.beans.factory.aware.BeanFactoryAware;
import org.zy.fluorite.beans.factory.exception.BeanCreationException;
import org.zy.fluorite.beans.factory.exception.NoSuchBeanDefinitionException;
import org.zy.fluorite.beans.factory.interfaces.AutowireCapableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.processor.InstantiationAwareBeanPostProcessor;
import org.zy.fluorite.beans.factory.support.DependencyDescriptor;
import org.zy.fluorite.beans.factory.support.InitDestroyAnnotationBeanPostProcessor;
import org.zy.fluorite.beans.factory.support.InjectionMetadata;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.beans.interfaces.PropertyValues;
import org.zy.fluorite.core.annotation.Lazy;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.subject.ExecutableParameter;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午2:16:25;
 * @Description
 */
@SuppressWarnings("serial")
public class CommonAnnotationBeanPostProcessor extends InitDestroyAnnotationBeanPostProcessor
		implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {

	private final transient Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

	private transient BeanFactory beanFactory;

	/** 忽略的注解标注类型 */
	private final Set<String> ignoredResourceTypes = new HashSet<>(1);

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, BeanDefinition beanDefinition)
			throws BeansException {
		InjectionMetadata metadata = findResourceMetadata(beanDefinition, bean.getClass(), pvs);
		String beanName = beanDefinition.getBeanName();
		try {
			metadata.inject(bean, beanName, pvs);
		} catch (Throwable ex) {
			throw new BeanCreationException("依赖注入失败，by bean：" + beanName, ex);
		}
		return pvs;
	}

	private InjectionMetadata findResourceMetadata(BeanDefinition beanDefinition, Class<? extends Object> clz,
			PropertyValues pvs) {
		String beanName = beanDefinition.getBeanName();
		String cacheKey = (Assert.hasText(beanName) ? beanName : clz.getName());
		InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);

		if (InjectionMetadata.needsRefresh(metadata, clz)) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(cacheKey);
				if (InjectionMetadata.needsRefresh(metadata, clz)) {
					metadata = buildResourceMetadata(beanDefinition, clz);
					this.injectionMetadataCache.put(cacheKey, metadata);
				}
			}
		}
		return metadata;
	}

	private InjectionMetadata buildResourceMetadata(BeanDefinition beanDefinition, Class<? extends Object> clz) {
		List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
		AnnotationMetadata annotationMetadata = beanDefinition.getAnnotationMetadata();
		Class<?> targetClass = clz;

		do {
			final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();
			
			ReflectionUtils.doWithLocalFields(targetClass, field -> {
				if (field.getAnnotations().length == 0)
					return;

				Resource resource = annotationMetadata.getAnnotationForField(field, Resource.class);
				if (resource != null) {
					if (Modifier.isStatic(field.getModifiers())) {
						throw new IllegalStateException("@Resource注解不能标注于静态属性");
					}
					if (!this.ignoredResourceTypes.contains(field.getType().getName())) {
						// 设置属性值不依赖于setter方法
//						PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(field, clz);
						currElements.add(new ResourceElement(field, field, null));
					}
				}
			});

			// 检查setter方法上是否标注了@Resource注解
			ReflectionUtils.doWithLocalMethods(targetClass, method -> {
				if (method.getAnnotations().length == 0)
					return;

				Resource resource = annotationMetadata.getAnnotationForMethod(method, Resource.class);
				if (resource != null) {
					if (Modifier.isStatic(method.getModifiers())) {
						throw new IllegalStateException("@Resource注解不能标注于静态方法");
					}
					Class<?>[] paramTypes = method.getParameterTypes();
					if (paramTypes.length != 1) {
						throw new IllegalStateException("@Resource标注的方法必须仅有一个参数，by method: " + method);
					}
					if (!this.ignoredResourceTypes.contains(paramTypes[0].getName())) {
						currElements.add(new ResourceElement(method, method.getParameters()[0], null));
					}
				}
			});

			elements.addAll(currElements);
			targetClass = targetClass.getSuperclass();
		} while (targetClass != null && targetClass != Object.class);

		return new InjectionMetadata(clz, elements);
	}

	protected Object buildLazyResourceProxy(final LookupElement element, final String requestingBeanName) {
		TargetSource ts = new TargetSource() {
			@Override
			public Class<?> getTargetClass() {
				return element.lookupType;
			}

			@Override
			public boolean isStatic() {
				return false;
			}

			@Override
			public Object getTarget() {
				return autowireResource(element, requestingBeanName);
			}

			@Override
			public void releaseTarget(Object target) {}
		};
		ProxyFactory pf = new ProxyFactory();
		pf.setTargetSource(ts);
		if (element.lookupType.isInterface()) {
			pf.addInterface(element.lookupType);
		}
		ClassLoader classLoader = (this.beanFactory instanceof ConfigurableBeanFactory
				? ((ConfigurableBeanFactory) this.beanFactory).getBeanClassLoader()
				: null);
		return pf.getProxy(classLoader);
	}

	/**
	 * 根据LookupElement的name进行自动注入
	 * 
	 * @param element            - 当前bean中标注了@Resource注解的属性或方法包装对象
	 * @param requestingBeanName - 应用当前后处理器的beanName
	 * @return
	 * @throws NoSuchBeanDefinitionException
	 */
	protected Object autowireResource(LookupElement element, String requestingBeanName)
			throws NoSuchBeanDefinitionException {

		Object resource;
		Set<String> autowiredBeanNames;
		// 获得@Resource的value值或者属性名，亦或者方法参数名
		String name = element.name;

		if (this.beanFactory instanceof AutowireCapableBeanFactory) {
			AutowireCapableBeanFactory beanFactory = (AutowireCapableBeanFactory) this.beanFactory;
			// 根据包装element属性还是方法调用不同的构造器
			DependencyDescriptor descriptor = element.getDependencyDescriptor();
			if (element.isDefaultName && !beanFactory.containsBean(name)) {
				autowiredBeanNames = new LinkedHashSet<>();
				resource = beanFactory.resolveDependency(descriptor, requestingBeanName, autowiredBeanNames);
				if (resource == null) {
					throw new NoSuchBeanDefinitionException("不可分辨的BeanFactory实现，by：" + element.getLookupType());
				}
			} else {
				resource = beanFactory.resolveBeanByName(name, descriptor);
				autowiredBeanNames = Collections.singleton(name);
			}
		} else {
			// 从IOC容器中获得指定名称和类型的bean
			resource = this.beanFactory.getBean(name, element.lookupType);
			autowiredBeanNames = Collections.singleton(name);
		}

		if (this.beanFactory instanceof ConfigurableBeanFactory) {
			ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) this.beanFactory;
			for (String autowiredBeanName : autowiredBeanNames) {
				if (requestingBeanName != null && beanFactory.containsBean(autowiredBeanName)) {
					// 将autowiredBeanName注册为requestingBeanName的依赖项
					beanFactory.registerDependentBean(autowiredBeanName, requestingBeanName);
				}
			}
		}

		return resource;
	}

	protected abstract class LookupElement extends InjectionMetadata.InjectedElement {
		protected String name = "";

		protected boolean isDefaultName = false;

		protected Class<?> lookupType = Object.class;

		protected String mappedName;

		public LookupElement(Member member, PropertyDescriptor pd) {
			super(member, pd);
		}

		public final String getName() {
			return this.name;
		}

		public final Class<?> getLookupType() {
			return this.lookupType;
		}

		public final DependencyDescriptor getDependencyDescriptor() {
			if (this.isField) {
				return new LookupDependencyDescriptor((Field) this.member, this.lookupType);
			} else {
				return new LookupDependencyDescriptor((Method) this.member, this.lookupType);
			}
		}
	}

	private class ResourceElement extends LookupElement {

		private final boolean lazyLookup;

		/**
		 * AnnotatedElement为方法或属性的类，主要定义了注解操作的相关方法
		 * 
		 * @param member
		 * @param ae
		 * @param pd
		 */
		public ResourceElement(Member member, AnnotatedElement ae, PropertyDescriptor pd) {
			super(member, pd);
			Resource resource = ae.getAnnotation(Resource.class);
			String resourceName = resource.name();
			Class<?> resourceType = resource.type();
			// resourceName为空串则为true
			this.isDefaultName = !Assert.hasText(resourceName);
			if (this.isDefaultName) {
				// 注解未指定名称则使用属性名或参数名填充
				resourceName = super.isField ? this.member.getName() : ((Parameter) ae).getName();
				if (super.isField && resourceName.startsWith("set") && resourceName.length() > 3) {
					resourceName = Introspector.decapitalize(resourceName.substring(3));
				}
			}
			if (Object.class != resourceType) {
				checkResourceType(resourceType);
			} else {
				// 没有指定源对象的类型则检查field或者method.
				resourceType = getResourceType();
			}
			this.name = (resourceName != null ? resourceName : "");
			this.lookupType = resourceType;
			String lookupValue = resource.lookup();
			this.mappedName = (Assert.hasText(lookupValue) ? lookupValue : resource.mappedName());
			Lazy lazy = ae.getAnnotation(Lazy.class);
			this.lazyLookup = (lazy != null && lazy.value());
		}

		@Override
		protected Object getResourceToInject(Object target, String requestingBeanName) {
			return (this.lazyLookup ? buildLazyResourceProxy(this, requestingBeanName)
					: autowireResource(this, requestingBeanName));
		}
	}

	/**
	 * 包装方法或属性
	 * @author PC
	 */
	private static class LookupDependencyDescriptor extends DependencyDescriptor {

		private final Class<?> lookupType;

		public LookupDependencyDescriptor(Field field, Class<?> lookupType) {
			super(field, true);
			this.lookupType = lookupType;
		}

		public LookupDependencyDescriptor(Method method, Class<?> lookupType) {
			super(new ExecutableParameter(method, 0), true);
			this.lookupType = lookupType;
		}

		public Class<?> getDependencyType() {
			return this.lookupType;
		}
	}
}

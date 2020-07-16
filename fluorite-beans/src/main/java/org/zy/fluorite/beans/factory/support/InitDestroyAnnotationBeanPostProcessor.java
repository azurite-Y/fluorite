package org.zy.fluorite.beans.factory.support;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.exception.BeanCreationException;
import org.zy.fluorite.beans.factory.interfaces.processor.DestructionAwareBeanPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.MergedBeanDefinitionPostProcessor;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.instantiation.InitializingBean;
import org.zy.fluorite.core.utils.ClassUtils;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @author: zy(azurite-Y);
 * @param <LifecycleMetadata>
 * @DateTime: 2020年6月4日 下午4:06:59;
 * @Description
 */
@SuppressWarnings({ "unused", "serial" })
public class InitDestroyAnnotationBeanPostProcessor
		implements DestructionAwareBeanPostProcessor, MergedBeanDefinitionPostProcessor, Serializable {

	protected transient Logger logger = LoggerFactory.getLogger(getClass());

	private Class<? extends Annotation> initAnnotationType = PostConstruct.class;

	private Class<? extends Annotation> destroyAnnotationType = PreDestroy.class;

	private final transient Map<Class<?>, LifecycleMetadata> lifecycleMetadataCache = new ConcurrentHashMap<>(256);

	// get、set
	public Class<? extends Annotation> getInitAnnotationType() {
		return initAnnotationType;
	}

	public void setInitAnnotationType(Class<? extends Annotation> initAnnotationType) {
		this.initAnnotationType = initAnnotationType;
	}

	public Class<? extends Annotation> getDestroyAnnotationType() {
		return destroyAnnotationType;
	}

	public void setDestroyAnnotationType(Class<? extends Annotation> destroyAnnotationType) {
		this.destroyAnnotationType = destroyAnnotationType;
	}

	@Override
	public void postProcessBeforeDestruction(Object bean, BeanDefinition definition) throws BeansException {
		LifecycleMetadata metadata = findLifecycleMetadata(definition);
		String beanName = definition.getBeanName();
		try {
			metadata.invokeDestroyMethods(bean, definition);
		} catch (InvocationTargetException ex) {
			logger.warn("调用销毁方法出现一个异常，by： '" + beanName + "'", ex);
		} catch (Throwable ex) {
			logger.warn("调用销毁方法失败，by： '" + beanName + "'", ex);
		}
	}

	@Override
	public boolean requiresDestruction(Object bean, BeanDefinition definition) {
		return findLifecycleMetadata(definition).hasDestroyMethods();
	}

	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		LifecycleMetadata metadata = findLifecycleMetadata(beanDefinition);
		metadata.checkConfigMembers(beanDefinition);
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, BeanDefinition beanDefinition) throws BeansException {
		LifecycleMetadata metadata = findLifecycleMetadata(beanDefinition);
		String beanName = beanDefinition.getBeanName();
		try {
			metadata.invokeInitMethods(bean, beanDefinition);
		} catch (InvocationTargetException ex) {
			throw new BeanCreationException("调用init方法失败，by：" + beanName, ex.getTargetException());
		} catch (Throwable ex) {
			throw new BeanCreationException("调用init方法失败，by：" + beanName, ex);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, BeanDefinition definition) throws BeansException {
		return bean;
	}

	/**
	 * 根据类对象查找生命周期元数据缓存，若有结果则返回无加过则根据此Class对象创建LifecycleMetadata对象
	 * 
	 * @param definition
	 * @return
	 */
	private LifecycleMetadata findLifecycleMetadata(BeanDefinition definition) {
		LifecycleMetadata metadata = this.lifecycleMetadataCache.get(definition.getBeanClass());
		if (metadata == null) { // 双重检测锁
			synchronized (this.lifecycleMetadataCache) {
				metadata = this.lifecycleMetadataCache.get(definition.getBeanClass());
				if (metadata == null) {
					metadata = buildLifecycleMetadata(definition);
					this.lifecycleMetadataCache.put(definition.getBeanClass(), metadata);
				}
				return metadata;
			}
		}
		return metadata;
	}

	/**
	 * 根据Class对象查找初始化方法和销毁方法
	 * 
	 * @param definition
	 * @return
	 */
	private LifecycleMetadata buildLifecycleMetadata(final BeanDefinition definition) {
		List<LifecycleElement> initMethods = new ArrayList<>();
		List<LifecycleElement> destroyMethods = new ArrayList<>();
		AnnotationMetadata annotationMetadata = definition.getAnnotationMetadata();
		Class<?> targetClass = definition.getBeanClass();
		do {
			final List<LifecycleElement> currInitMethods = new ArrayList<>();
			final List<LifecycleElement> currDestroyMethods = new ArrayList<>();

			ReflectionUtils.doWithLocalMethods(targetClass, method -> {
				if (method.getAnnotations().length == 0)	return;

				if (this.initAnnotationType != null	&& annotationMetadata.isAnnotatedForMethod(method, initAnnotationType)) {
					DebugUtils.log(logger, "在"+method.getDeclaringClass().getName()+"'类中找到初始化方法："+method.getName());
					currInitMethods.add(new LifecycleElement(method));
				}
				if (this.destroyAnnotationType != null && annotationMetadata.isAnnotatedForMethod(method, this.destroyAnnotationType)) {
					DebugUtils.log(logger, "在"+method.getDeclaringClass().getName()+"'类中找到销毁方法："+method.getName());
					currDestroyMethods.add(new LifecycleElement(method));
				}
			});

			initMethods.addAll(currInitMethods);
			destroyMethods.addAll(currDestroyMethods);
			// 检查父类
			targetClass = targetClass.getSuperclass();
		} while (targetClass != null && targetClass != Object.class);

		return new LifecycleMetadata(definition.getBeanClass(), initMethods, destroyMethods);
	}

	/**
	 * 统筹一个类之中定义的初始化和销毁方法
	 */
	private class LifecycleMetadata {
		private final Class<?> targetClass;

		private final List<LifecycleElement> initMethods;

		private final List<LifecycleElement> destroyMethods;

		public LifecycleMetadata(Class<?> targetClass) {
			super();
			this.targetClass = targetClass;
			initMethods = new ArrayList<>();
			destroyMethods = new ArrayList<>();
		}

		public LifecycleMetadata(Class<?> targetClass, List<LifecycleElement> initMethods,
				List<LifecycleElement> destroyMethods) {
			this.targetClass = targetClass;
			this.initMethods = initMethods;
			this.destroyMethods = destroyMethods;
		}

		/**
		 * 检查初始化方法和销毁集合，注册未注册到BeanDefinition中的初始化和销毁方法
		 * 
		 * @param beanDefinition
		 */
		public void checkConfigMembers(RootBeanDefinition beanDefinition) {
			String name = this.targetClass.getName();
			for (LifecycleElement element : this.initMethods) {
				String methodIdentifier = element.getIdentifier();
				if (!beanDefinition.isInitMethod(methodIdentifier)) {
					element.setChecked(true);
					beanDefinition.registerInitMethod(methodIdentifier);
					if (DebugUtils.debug) {
						logger.info("注册init方法，by：{}", name);
					}
				}
			}

			for (LifecycleElement element : this.destroyMethods) {
				String methodIdentifier = element.getIdentifier();
				if (!beanDefinition.isDestroyMethod(methodIdentifier)) {
					element.setChecked(true);
					beanDefinition.registerDestroyMethod(methodIdentifier);
					if (DebugUtils.debug) {
						logger.info("注册destroy方法，by：{}" , name);
					}
				}
			}
		}

		public void invokeInitMethods(Object target, BeanDefinition beanDefinition) throws Throwable {
			boolean InitializingBean = InitializingBean.class.isInstance(target);
			if (!this.initMethods.isEmpty()) {
				for (LifecycleElement element : initMethods) {
					if (InitializingBean && element.getIdentifier().equals("afterPropertiesSet")) {
						continue ;
					}
					if (element.isChecked()) {
						element.invoke(target);
						boolean remove = beanDefinition.removeInitMethod(element.getIdentifier());
						DebugUtils.log(logger, "调用init方法"+(remove? "成功" :"失败" )+"，by：" + this.targetClass.getName());
					}
				}
			}
		}

		public void invokeDestroyMethods(Object target, BeanDefinition beanDefinition) throws Throwable {
			if (!this.destroyMethods.isEmpty()) {
				for (LifecycleElement element : destroyMethods) {
					if (element.isChecked()) {
						element.invoke(target);
						boolean remove = beanDefinition.removeDestroyMethods(element.getIdentifier());
						DebugUtils.log(logger, "调用destroy方法"+(remove? "成功" :"失败" )+"，by："+this.targetClass.getName());
					}
				}
			}
		}

		public boolean hasDestroyMethods() {
			return !this.destroyMethods.isEmpty();
		}
	}

	/**
	 * 封装具体初始化和销毁方法
	 * 
	 * @author PC
	 *
	 */
	private class LifecycleElement {

		private final Method method;

		private final String identifier;

		/** 是否已检查 */
		private boolean isChecked = false;

		public LifecycleElement(Method method) {
			if (method.getParameterCount() != 0) {
				throw new IllegalStateException("生命周期方法只能为无参方法，by method：" + method);
			}
			this.method = method;
			this.identifier = (Modifier.isPrivate(method.getModifiers()) ? ClassUtils.getQualifiedMethodName(method)
					: method.getName());
		}

		public Method getMethod() {
			return this.method;
		}

		public String getIdentifier() {
			return this.identifier;
		}

		public boolean isChecked() {
			return isChecked;
		}

		public void setChecked(boolean isChecked) {
			this.isChecked = isChecked;
		}

		public void invoke(Object target) throws Throwable {
			ReflectionUtils.makeAccessible(this.method);
//			DebugUtils.log(logger, "生命周期方法调用："+method.getName()+"，by："+target);
			this.method.invoke(target);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof LifecycleElement)) {
				return false;
			}
			LifecycleElement otherElement = (LifecycleElement) other;
			return (this.identifier.equals(otherElement.identifier));
		}

		@Override
		public int hashCode() {
			return this.identifier.hashCode();
		}
	}

}

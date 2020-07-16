package org.zy.fluorite.aop.aspectj.support;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.zy.fluorite.aop.aspectj.interfaces.AspectJAdvisorFactory;
import org.zy.fluorite.aop.aspectj.interfaces.MetadataAwareAspectInstanceFactory;
import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.annotation.Lazy;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月5日 下午3:31:20;
 * @author zy(azurite-Y);
 * @Description 帮助程序，用于从BeanFactory检索切面bean，并基于它们构建Advisors，用于自动代理。
 */
public class BeanFactoryAspectJAdvisorsBuilder {
	private final ConfigurableListableBeanFactory beanFactory;

	/** 生成Advisor的工厂 */
	private final AspectJAdvisorFactory advisorFactory;

	/** 切面 Bean 名称 */
	private volatile List<String> aspectBeanNames;

	/** Advisor缓存 */
	private final Map<String, List<Advisor>> advisorsCache = new ConcurrentHashMap<>();

	/** 切面实例缓存 */
	private final Map<String, MetadataAwareAspectInstanceFactory> aspectFactoryCache = new ConcurrentHashMap<>();

	public BeanFactoryAspectJAdvisorsBuilder(ConfigurableListableBeanFactory beanFactory) {
		this(beanFactory, new ReflectiveAspectJAdvisorFactory(beanFactory));
	}

	public BeanFactoryAspectJAdvisorsBuilder(ConfigurableListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
		super();
		Assert.notNull(beanFactory, "eanFactory不能为null");
		Assert.notNull(advisorFactory, "advisorFactory不能为null");
		this.beanFactory = beanFactory;
		this.advisorFactory = advisorFactory;
	}

	public Collection<? extends Advisor> buildAspectJAdvisors() {
		List<String> aspectNames = this.aspectBeanNames;
		if (aspectNames == null) {
			synchronized (this) {
				aspectNames = this.aspectBeanNames;
				if (aspectNames == null) {
					List<Advisor> advisors = new LinkedList<>();
					aspectNames = new LinkedList<>();
					// 获得所有注册Bean的BeanName
					String[] beanNames = beanFactory.getBeanNamesForType(Object.class, true, false);
					BeanDefinition bd = null;
					
					for (String beanName : beanNames) {
						// 包括了FactoryBean创建的实例
						Class<?> beanType = this.beanFactory.getType(beanName);
						bd = beanFactory.getBeanDefinition(beanName);
						AnnotationMetadata metadata = bd.getAnnotationMetadata();
						if (beanType == null) {
							continue;
						}
						
						// 检查beanType是否标注了@Aspect注解
						if (this.advisorFactory.isAspect(metadata)) {
							aspectNames.add(beanName);
							if ( !metadata.isAnnotatedForClass(Lazy.class)) { // 切面未标注@Lazy注解则是单例的
								MetadataAwareAspectInstanceFactory factory = 
										new BeanFactoryAspectInstanceFactory(this.beanFactory , beanName , metadata , beanType);
								List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
								if (this.beanFactory.isSingleton(beanName)) {
									this.advisorsCache.put(beanName, classAdvisors);
								} else {
									this.aspectFactoryCache.put(beanName, factory);
								}
								advisors.addAll(classAdvisors);
							} else {
								if (this.beanFactory.isSingleton(beanName)) {
									throw new IllegalArgumentException("Bean是单例的，但是切面实例化模型不是单例的");
								}
								MetadataAwareAspectInstanceFactory factory = 
										new PrototypeAspectInstanceFactory(this.beanFactory , beanName,metadata,beanType);
								this.aspectFactoryCache.put(beanName, factory);
								advisors.addAll(this.advisorFactory.getAdvisors(factory));
							}
						}
					}
					// 所有Bean都检查完之后保存切面类的简易类名（不包含包名）
					this.aspectBeanNames = aspectNames;
					return advisors;
				}
			}
		}

		if (aspectNames.isEmpty()) {
			return Collections.emptyList();
		}

		// 到此则表示之前已检查过所有Bean，并筛选出了切面bean
		List<Advisor> advisors = new LinkedList<>();
		for (String aspectName : aspectNames) {
			// 从切面类缓存中获得对应的切面类方法集合
			List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
			if (cachedAdvisors != null) { // 单例
				advisors.addAll(cachedAdvisors);
			} else { // 非单例
				MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
				// 重新解析切面Bean信息生成Advisor实例
				advisors.addAll(this.advisorFactory.getAdvisors(factory));
			}
		}
		return advisors;

	}

}

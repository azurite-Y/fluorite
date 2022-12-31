package org.zy.fluorite.context.annotation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.support.AnnotationAwareOrderComparator;
import org.zy.fluorite.context.annotation.conditional.Conditional;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnBean;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnClass;
import org.zy.fluorite.context.annotation.interfaces.Condition;
import org.zy.fluorite.context.annotation.interfaces.ConditionContext;
import org.zy.fluorite.context.annotation.interfaces.ConfigurationCondition;
import org.zy.fluorite.context.annotation.interfaces.ConfigurationCondition.ConfigurationPhase;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;
import org.zy.fluorite.core.environment.StandardEnvironment;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.environment.interfaces.EnvironmentCapable;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年6月20日 下午1:10:38;
 * @author zy(azurite-Y);
 * @Description 条件注解处理器
 */
public class ConditionEvaluator {
	private final Map<ConfigurationClass, Boolean> skipped = new HashMap<>();
	private final ConditionContextImpl context;
	public final Logger logger = LoggerFactory.getLogger(getClass());

	public ConditionEvaluator(BeanDefinitionRegistry registry, Environment environment) {
		this.context = new ConditionContextImpl(registry, environment);
	}

	/**
	 * 判断当前类或方法是否标注了@Conditional，若标注则根据@Conditional注释确定是否应跳过此项
	 * <p>
	 * 因为 {@linkplain Condition} 的实现类一般是由诸如 {@linkplain ConditionalOnBean} 或
	 * {@linkplain ConditionalOnClass} 这样的条件注解所引入。<br/>
	 * 在{@linkplain ConditionEvaluator}中，同一个处理周期就意味着可能要处理多个 {@link Conditional} 注解。
	 * 而由其引入的 {@linkplain Condition} 实现类又不可控的可能出现重复。<br/>
	 * 所以特规定：同一个处理周期中每个{@linkplain Condition} 实现只会被调用一次matches方法。 <br/>
	 * <p/>
	 * 
	 * @param attributes - 根据attributes参数的从属不同而区分类和方法的条件判断
	 * @param phase
	 * @return true则应跳过，false则不应跳过
	 */
	public boolean shouldSkip(AnnotationAttributes attributes, ConfigurationPhase phase) {
		Assert.notNull(phase, "ConfigurationPhase 枚举不可为null");
		List<Conditional> annotations = attributes.getAnnotationList(Conditional.class);
		if (!Assert.notNull(annotations)) {
			return false;
		}

		Set<Class<? extends ConfigurationCondition>> values = new HashSet<>();
		for (Conditional conditional : annotations) {
			for (Class<? extends ConfigurationCondition> clz : conditional.value()) {
				// 只会存储一个Condition实现的类型，同类型会被覆盖
				values.add(clz);
			}
		}
		AnnotationAwareOrderComparator.sort(values);
		
		for (Class<? extends ConfigurationCondition> value : values) {
			ConfigurationCondition condition = ReflectionUtils.instantiateClass(value);
			DebugUtils.log(logger,"要使用的Condition实现：" + condition.getClass().getSimpleName() + "，注解标注位置：" + attributes.getElement());
			
			if (condition.getConfigurationPhase() == phase) { // 评估阶段筛选
				if (!condition.matcher(this.context, attributes)) {
					// 若条件评估结果是应该跳过则直接返回结果
					return true;
				} 
			}
		}
		return false;
	}

	/**
	 * 判断导入此类的配置类是否需被排除，若需要则排除此配置类
	 * 
	 * @param configClass
	 * @return true则应跳过，false则不应跳过
	 */
	public boolean shouldSkip(ConfigurationClass configClass) {
		// 在忽略缓存中查找忽略结果
		Boolean skip = this.skipped.get(configClass);
		if (skip == null) {
			if (configClass.isImported()) { // 递归检查内部配置类和导入类
				boolean allSkipped = true;
				for (ConfigurationClass importedBy : configClass.getImportedBy()) {
					if (!shouldSkip(importedBy)) { // 若需跳过此导入类则继续检查下一个导入类
						allSkipped = false;
						break;
					}
				}
				if (allSkipped) {
					// 导入此导入类的类都被跳过，因此跳过此导入类
					skip = true;
				}
			}
			if (skip == null) {
				skip = shouldSkip(configClass.getAnnotationMetadata().getAnnotationAttributesForClass(), ConfigurationPhase.REGISTER_BEAN);
			}
			// 存储排除结果
			this.skipped.put(configClass, skip);
		}
		return skip;
	}

	/**
	 * 条件上下文的实现
	 */
	private static class ConditionContextImpl implements ConditionContext {
		private final BeanDefinitionRegistry registry;

		private final ConfigurableListableBeanFactory beanFactory;

		private final Environment environment;

		private final ClassLoader classLoader;

		public ConditionContextImpl(BeanDefinitionRegistry registry, Environment environment) {
			this.registry = registry;
			this.beanFactory = deduceBeanFactory(registry);
			this.environment = (environment != null ? environment : deduceEnvironment(registry));
			this.classLoader = deduceClassLoader(this.beanFactory);
		}

		private ConfigurableListableBeanFactory deduceBeanFactory(BeanDefinitionRegistry source) {
			if (source instanceof ConfigurableListableBeanFactory) {
				return (ConfigurableListableBeanFactory) source;
			}
			if (source instanceof ConfigurableApplicationContext) {
				return (((ConfigurableApplicationContext) source).getBeanFactory());
			}
			return null;
		}

		private Environment deduceEnvironment(BeanDefinitionRegistry source) {
			if (source instanceof EnvironmentCapable) {
				return ((EnvironmentCapable) source).getEnvironment();
			}
			return new StandardEnvironment();
		}

		private ClassLoader deduceClassLoader(ConfigurableListableBeanFactory beanFactory) {
			if (beanFactory != null) {
				return beanFactory.getBeanClassLoader();
			}
			return ClassUtils.getDefaultClassLoader();
		}

		@Override
		public BeanDefinitionRegistry getRegistry() {
			Assert.isTrue(this.registry != null, "没有可用的BeanDefinitionRegistry实现");
			return this.registry;
		}

		@Override
		public ConfigurableListableBeanFactory getBeanFactory() {
			return this.beanFactory;
		}

		@Override
		public Environment getEnvironment() {
			return this.environment;
		}

		@Override
		public ClassLoader getClassLoader() {
			return this.classLoader;
		}
	}
}

package org.zy.fluorite.context.annotation;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.factory.exception.BeanDefinitionStoreException;
import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.support.BeanMethod;
import org.zy.fluorite.beans.factory.support.SourceClass;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.beans.support.AnnotationAwareOrderComparator;
import org.zy.fluorite.context.annotation.interfaces.ConfigurationCondition.ConfigurationPhase;
import org.zy.fluorite.context.annotation.interfaces.DeferredImportSelector;
import org.zy.fluorite.context.annotation.interfaces.ImportBeanDefinitionRegistrar;
import org.zy.fluorite.context.annotation.interfaces.ImportSelector;
import org.zy.fluorite.context.exception.BeanDefinitionParsingException;
import org.zy.fluorite.context.utils.ConfigurationClassUtils;
import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.annotation.ComponentScan;
import org.zy.fluorite.core.annotation.Configuration;
import org.zy.fluorite.core.annotation.Import;
import org.zy.fluorite.core.annotation.PropertySource;
import org.zy.fluorite.core.annotation.PropertySources;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.environment.interfaces.PropertySource.SimplePropertySource;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.AnnotationUtils;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.PropertiesUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年6月19日 下午5:45:11;
 * @author zy(azurite-Y);
 * @param <E>
 * @Description 分析配置类定义，填充配置类对象的集合（分析单个配置类可能导致配置类对象的数量增多，因为一个配置类可能使用导入批注导入另一个配置类）
 */
public class ConfigurationClassParser {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/** 解析延迟导入选择器的Ordered接口或@Order注解确定其顺序 */
	private static final Comparator<DeferredImportSelectorHolder> DEFERRED_IMPORT_COMPARATOR = (o1,
			o2) -> AnnotationAwareOrderComparator.INSTANCE.compare(o1.getImportSelector(), o2.getImportSelector());

	private final Environment environment;

	private final BeanDefinitionRegistry registry;

	/** 组件扫描 */
	private final ComponentScanAnnotationParser componentScanParser;

	/** @Condition 及其扩展注解鉴别器 */
	private final ConditionEvaluator conditionEvaluator;

	/** 存储导入类、内部配置类和其他配置类的类对象与ConfigurationClass对象映射 */
	private final Map<Class<?>, ConfigurationClass> configurationClasses = new LinkedHashMap<>();

	/** 已知的父类 */
	private final Map<String, ConfigurationClass> knownSuperclasses = new HashMap<>();

	private final ArrayDeque<SourceClass> importStack = new ArrayDeque<>();

	private final DeferredImportSelectorHandler deferredImportSelectorHandler = new DeferredImportSelectorHandler();

	public ConfigurationClassParser(Environment environment, BeanDefinitionRegistry registry,
			ConditionEvaluator conditionEvaluator) {
		this.environment = environment;
		this.registry = registry;
		this.componentScanParser = new ComponentScanAnnotationParser(environment, registry);
		this.conditionEvaluator = conditionEvaluator;
	}

	public ArrayDeque<SourceClass> getImportStack() {
		return this.importStack;
	}

	public void parse(Set<BeanDefinition> candidates) {
		for (BeanDefinition beanDefinition : candidates) {
			processConfigurationClass(new ConfigurationClass(beanDefinition));
		}
		this.deferredImportSelectorHandler.process();
	}

	/**
	 * 验证配置类和Bean方法的有效性 配置类不能被final修饰 静态@Bean方法没有要验证的约束
	 * 
	 * @Bean 标记的方法不能为静态、final或private
	 */
	public void validate(Logger logger) {
		for (Class<?> configClass : this.configurationClasses.keySet()) {
			ConfigurationClass configurationClass = this.configurationClasses.get(configClass);
			configurationClass.validate(logger);
		}
	}

	/**
	 * 解析指定类的注解信息
	 * 
	 * @param configClass
	 * @throws IOException
	 */
	protected void processConfigurationClass(ConfigurationClass configClass) {
		// 判断当前类是否标注了@Conditional，若标注则根据@Conditional注释确定是否应跳过此项
		if (this.conditionEvaluator.shouldSkip(configClass.getAnnotationMetadata().getAnnotationAttributesForClass(),
				ConfigurationPhase.PARSE_CONFIGURATION)) {
			return;
		}

		// 递归处理配置类及其超类层次结构.
		SourceClass sourceClass = configClass.getSourceClass();
		do {
			sourceClass = doProcessConfigurationClass(configClass, sourceClass);
		} while (sourceClass != null);
		this.configurationClasses.put(configClass.getSource(), configClass);
	}

	/**
	 * @param configClass 代表当前检查类，如导入类、内部配置类
	 * @param source      - 为configClass的父类或configClass本身
	 * @return
	 */
	protected SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass source) {
		/**
		 * 递归地首先处理任何成员（嵌套）类，注册恰好是配置类本身的成员（嵌套）类 1.
		 * 首先根据sourceClass获得类中定义的公共、私有、保护的内部类，不包括父类与父接口 2.
		 * 检查这些使用@Order注解或Ordered接口排序，接着判断排序好的内部类是否标注了@Configuration注解，
		 * 若标注了且未存在以importStack容器中则将此内部类加入到importStack容器中并调用
		 * #processConfigurationClass()方法，
		 */
		processMemberClasses(configClass, source);

		// 处理标注在类上的任何@PropertySource注解
		if (this.environment instanceof ConfigurableEnvironment) {
			processPropertySource(source);
		} else {
			logger.warn("忽略@PropertySource 注解，因为 Environment 必须实现 ConfigurableEnvironment接口，by： " + source.getName());
		}

		AnnotationMetadata annotationMetadata = source.getAnnotationMetadata();
		/**
		 * 处理任何@ComponentScan注解
		 */
		List<ComponentScan> componentScans = AnnotationUtils.findComponentScan(annotationMetadata);
		// shouldSkip()：根据@Conditional注释确定是否应跳过项
		if (!componentScans.isEmpty()
				&& !this.conditionEvaluator.shouldSkip(annotationMetadata.getAnnotationAttributesForClass(), ConfigurationPhase.PARSE_CONFIGURATION)) {
			for (ComponentScan componentScan : componentScans) {
				Set<BeanDefinition> scannedBeanDefinitions = this.componentScanParser.parse(componentScan, source);
				// 检查扫描到定义集以获取任何进一步的配置类，并在需要时递归解析
				for (BeanDefinition bd : scannedBeanDefinitions) {
					// 判断扫描到的BeanDefinition是否需要解析相关注解【@Configuration、@Import、@Component、@ImportResource、@ComponentScan、@Bean、@Order】
					if (ConfigurationClassUtils.checkConfigurationClassCandidate(bd)) {
						// 递归解析扫描到标注了@Configuration
						// @PropertySource、@ComponentScan、@Import、@ImportResource、@Bean的类。
						processConfigurationClass(new ConfigurationClass(bd));
					}
				}
			}
		}

		/**
		 * 处理任何@Import注解
		 */
		processImports(configClass, source, getImports(source), true);

		/** 处理任何@Bean注解 */
		Class<?> clz = source.getSource();
		ReflectionUtils.doWithLocalMethods(clz, method -> {
			if (method.getAnnotations().length == 0) {
				return;
			}

			AnnotationAttributes attributesForMethod = annotationMetadata.getAnnotationAttributesForMethod(method);
			// 标注了@Bean注解且条件允许才保存Method对象
			if (!this.conditionEvaluator.shouldSkip(attributesForMethod, ConfigurationPhase.PARSE_CONFIGURATION)
					&& (attributesForMethod.getAnnotation(Bean.class) != null)) {
				configClass.addBeanMethod(method, attributesForMethod);
			}
		});

		/**
		 * 处理接口上的默认方法，递归检查Java 8+接口上的默认方法或其他具体方法
		 * 是否标注了@Bean注解，是则填充到ConfigClass对象的beanMethods容器中
		 */
		processInterfaces(configClass, source.getSource());

		/**
		 * 处理超类（如果有）
		 * 从sourceClass中获得全称类名不以java开头的父类信息，若存在则保存到本ConfigurationClassParser（配置类处理器）的knownSuperclasses(已知的超类)集合中
		 * 返回父类的SourceClass对象，若不存在则返回null。这么做方便递归的处理父类的注解信息
		 */
		Class<?> superclass = source.getSource().getSuperclass();
		if (superclass != null) {
			String superClassName = superclass.getName();
			if (!superClassName.startsWith("java") && !this.knownSuperclasses.containsKey(superClassName)) {
				this.knownSuperclasses.put(superClassName, configClass);
				// 找到超类，返回超类的Class对象
				return new SourceClass(superclass);
			}
		}
		// 没有超类->处理完成
		return null;
	}

	private void processInterfaces(ConfigurationClass configClass, Class<?> source) {
		Class<?>[] interfaces = source.getInterfaces();
		for (Class<?> clz : interfaces) {
			ReflectionUtils.doWithLocalMethods(clz, method -> {
				if (method.getAnnotations().length == 0) {
					return;
				}

				BeanMethod beanMethod = new BeanMethod(method);
				AnnotationAttributes attributesForMethod = beanMethod.getAnnotationAttributesForMethod();
				if (attributesForMethod.getAnnotation(Bean.class) != null) {
					if (method.isDefault() && DebugUtils.debug) {
						logger.info("忽略在接口中找到的@Bean标注方法，理由：此方法为默认方法，by interface：" + clz.getName() + " method："+ method.getName());
						return;
					}
					configClass.addBeanMethod(beanMethod);
				}
			});
			// 检查当前接口的父接口
			processInterfaces(configClass, clz);
		}
	}

	/**
	 * 解析标注@Configuration注解的内部类
	 * 
	 * @param configClass
	 * @param source      - 代表当前检查类或当前检查类的父类
	 */
	private void processMemberClasses(ConfigurationClass configClass, SourceClass source) throws BeanDefinitionParsingException {
		List<ConfigurationClass> inners = this.parseConfigurationInnerClass(configClass, source);
		AnnotationAwareOrderComparator.sort(inners);
		for (ConfigurationClass innerClz : inners) {
			SourceClass innerSourceClass = innerClz.getSourceClass();
			if (this.importStack.contains(innerSourceClass)) {
				throw new BeanDefinitionParsingException("重复的内部配置类，by：" + innerClz.getSource().getName());
			} else {
				this.importStack.push(innerSourceClass);
				try {
					if (DebugUtils.debug) {
						logger.info("解析标注@Configuration注解的内部类：" + innerClz.getSource().getName());
					}
					processConfigurationClass(innerClz);
				} finally {
					this.importStack.pop();
				}
			}
		}
	}

	/**
	 * 解析标注@Configuration注解的内部类
	 * 
	 * @param function
	 */
	private List<ConfigurationClass> parseConfigurationInnerClass(ConfigurationClass configClass, SourceClass parent) {
		// 获得该类的内部类
		Class<?>[] declaredClasses = parent.getSource().getDeclaredClasses();
		AnnotationMetadata annotationMetadata = parent.getAnnotationMetadata();
		List<ConfigurationClass> list = new ArrayList<>();
		for (Class<?> clz : declaredClasses) {
			if (annotationMetadata.isAnnotatedForInnerClz(clz, Configuration.class) && !clz.getName().equals(configClass.getClassName())) {
				list.add(new ConfigurationClass(clz, null));
			}
		}
		return list;
	}

	/**
	 * 解析配置类标注的@PropertySource、@PropertySources注解
	 * 
	 * @param source
	 */
	private void processPropertySource(SourceClass source) {
		AnnotationMetadata annotationMetadata = source.getAnnotationMetadata();

		PropertySource annotation = annotationMetadata.getAnnotationForClass(PropertySource.class);
		if (annotation == null) {
			PropertySources propertySources = annotationMetadata.getAnnotationForClass(PropertySources.class);
			if (propertySources != null) {
				for (PropertySource propertySource : propertySources.value()) {
					doParsePropertySource(propertySource, source);
				}
			}
		} else {
			doParsePropertySource(annotation, source);
		}
	}

	private void doParsePropertySource(PropertySource propertySource, SourceClass source) {
		String name = propertySource.name();
		String[] value = propertySource.value();
		boolean ignoreResourceNotFound = propertySource.ignoreResourceNotFound();
		String encoding = propertySource.encoding();

		ConfigurableEnvironment ce = (ConfigurableEnvironment) this.environment;

		for (String path : value) {
			if (!Assert.hasText(name)) { // 未配置属性源名称
				StringBuilder builder = new StringBuilder();
				String[] tokenizeToStringArray = StringUtils.tokenizeToStringArray(path, "/", null);

				builder.append(source.getSimpleName());
				builder.append("-");
				// 获得配置文件名称
				builder.append(tokenizeToStringArray[tokenizeToStringArray.length - 1]);

				name = builder.toString();
			}
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Map<String, String> properties = (Map) PropertiesUtils.load(path, encoding, ignoreResourceNotFound);
			if (properties != null) {
				if (ce.containPropertySources(name)) {
					ce.getPropertySources().addLast(new SimplePropertySource(name, properties));
					if (DebugUtils.debug) {
						logger.info("@PropertySource配置的属性源注册成功，by name：" + name);
					}
				} else {
					logger.warn("忽略@PropertySource配置的属性源。理由：属性源名称重叠，请为此属性源指定其他名称，by name：" + name + " path：" + path);
				}
			} else {
				logger.warn("配置文件路径错误，by name：" + name + " path：" + path);
			}
		}
	}

	/**
	 * @param configClass - 代表当前检查类，如导入类、内部配置类
	 * @param source - 为configClass的父类
	 * @param set
	 * @param checkForCircularImports - 是否检查循环导入
	 */
	private void processImports(ConfigurationClass configClass, SourceClass source, Set<SourceClass> imports,
			boolean checkForCircularImports) {
		if (imports.isEmpty()) {
			return;
		}

		if (checkForCircularImports) {
			try {
				// 检查是否又导入了之前已处理的类
				for (SourceClass sourceClass : imports) {
					assetChainedImportOnStack(sourceClass);
				}
			} catch (BeanDefinitionParsingException e) {
				e.printStackTrace();
			}
		}

		try {
			for (SourceClass sourceClass : imports) {
				Class<?> importClass = sourceClass.getSource();
				if (ImportSelector.class.isAssignableFrom(importClass)) {
					// 实例化导入类
					ImportSelector selector = (ImportSelector) ReflectionUtils.instantiateClass(importClass);

					Assert.isTrue(this.registry instanceof ConfigurableListableBeanFactory, "'registry'必须实现ConfigurableListableBeanFactory接口");
					// 调用对应的Aware接口方法
					selector.invokeAwareMethods(this.environment, (ConfigurableListableBeanFactory) this.registry);
					if (this.deferredImportSelectorHandler != null && selector instanceof DeferredImportSelector) {
						// deferredImportSelectorHandler：存储和处理延迟导入选择器
						this.deferredImportSelectorHandler.handle(configClass, (DeferredImportSelector) selector, this.conditionEvaluator);
					} else { // 导入类未实现DeferredImportSelector
						Set<SourceClass> importClassNames = selector.selectImports(configClass.getAnnotationMetadata(), this.conditionEvaluator);
						// 解析当前导入类导入的类
						for (SourceClass importSourceClass : importClassNames) {
							processImports(configClass, importSourceClass, getImports(importSourceClass), false);
						}
					}
				} else if (ImportBeanDefinitionRegistrar.class.isAssignableFrom(importClass)) {
					// 实例化导入类
					ImportBeanDefinitionRegistrar registrar = (ImportBeanDefinitionRegistrar) ReflectionUtils.instantiateClass(importClass);
					
					// 将此导入类实例和根类的注解信息映射保存到当前ConfigurationClass的importBeanDefinitionRegistrars容器中
					configClass.addImportBeanDefinitionRegistrar(registrar, configClass.getAnnotationMetadata());
				} else {
					/**
					 * 候选类不是ImportSelector或ImportBeanDefinitionRegistrar->将其作为@Configuration类处理
					 * 解析此导入类的注解信息
					 */
					ConfigurationClass configurationClass = new ConfigurationClass(new SourceClass(importClass));
					// 设置导入依存关系
					configurationClass.addImportBy(configClass);
					processConfigurationClass(configurationClass);
				}
			}
		} catch (BeanDefinitionStoreException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new BeanDefinitionStoreException("无法处理配置类的导入候选项: [" + configClass.getClassName() + "]", ex);
		} finally {
//					this.importStack.pop();
		}
	}

	/**
	 * 从Class对象中获得@Import导入的Class对象集合
	 * 
	 * @param sourceClass
	 * @return
	 */
	private Set<SourceClass> getImports(SourceClass source) {
		return collectImports(source, new LinkedHashSet<>(), new LinkedHashSet<>());
	}

	/**
	 * 从Class对象中获得@Import导入的Class对象集合
	 * 
	 * @param source
	 * @param visited - 存储已查找的Class对象
	 * @param imports - 存储@Import导入的Class对象
	 * @return
	 */
	private Set<SourceClass> collectImports(SourceClass source, Set<SourceClass> imports, Set<SourceClass> visited) {
		if (visited.add(source)) {
			List<Import> importList = source.getAnnotationMetadata().getAnnotationListForClass(Import.class);
			if (importList != null) {
				for (Import im : importList) {
					for (Class<?> clz : im.value()) {
						SourceClass sourceClass = new SourceClass(clz);
						DebugUtils.log(logger, source.getName() + " 导入：" + clz.getName());
						imports.add(sourceClass);
						// 不在此处解析导入类逻辑，改为解析@Import注解标注类时解析导入关系，避免a导入b，b导入c而判定c被循环导入的问题
//						collectImports(sourceClass, imports, visited);
					}
				}
			}
		}
		return imports;
	}

	/**
	 * 循环导入断言，若出现循环导入则抛出异常
	 * 
	 * @param configClass - 当前检查类
	 * @param imports     - 通过@Import注解导入的类集合，在一次递归检查中检查类是不会存在于此集合的
	 * @throws BeanDefinitionParsingException
	 */
	private void assetChainedImportOnStack(SourceClass sourceClass) throws BeanDefinitionParsingException {
		if ( EnableConfigurationPropertiesRegistrar.class.equals(sourceClass.getSource()) ) {
			return ;
		}
		
		if (this.importStack.contains(sourceClass)) { // 条件成立则必定代表循环导入或使用@Import注解导入其他内部配置类
			for (SourceClass importSourceClass : importStack) { // 迭代此集合确定循环导入的类
				// 与导入类和内部配置类的Class对象比对
				if (importSourceClass.equals(sourceClass)) {
					throw new BeanDefinitionParsingException("循环导入，导入类不唯一。by ：" + importSourceClass);
				}
			}
		}
		
		// 将此导入类添加到导入类集合中
		this.importStack.push(sourceClass);
	}

	public Collection<ConfigurationClass> getConfigurationClasses() {
		return this.configurationClasses.values();
	}

	private class DeferredImportSelectorHandler {
		private List<DeferredImportSelectorHolder> deferredImportSelectors = new ArrayList<>();

		private ConditionEvaluator conditionEvaluator;

		/**
		 * 处理指定的DeferedImportSelector。 如果正在收集延迟导入选择器，则会将此实例注册到列表中。 如果正在处理它们，则
		 * DeferredImportSelector 也会根据其DeferredImportSelector.Group组.
		 * 
		 * @param configClass
		 * @param importSelector     - 延迟导入选择器
		 * @param conditionEvaluator
		 */
		public void handle(ConfigurationClass configClass, DeferredImportSelector importSelector, ConditionEvaluator conditionEvaluator) {
			if (this.conditionEvaluator == null) {
				this.conditionEvaluator = conditionEvaluator;
			}

			// 封装参数
			this.deferredImportSelectors.add(new DeferredImportSelectorHolder(configClass, importSelector));
		}

		/**
		 * 处理延迟导入选择器
		 */
		public void process() {
			if (this.deferredImportSelectors.isEmpty()) {
				return;
			}

			// 根据@Order注解排序
			deferredImportSelectors.sort(DEFERRED_IMPORT_COMPARATOR);

			for (DeferredImportSelectorHolder deferredImportSelectorHolder : this.deferredImportSelectors) {
				ConfigurationClass configurationClass = deferredImportSelectorHolder.getConfigurationClass();
				DeferredImportSelector importSelector = deferredImportSelectorHolder.getImportSelector();
				// 获得导入类的Class对象
				Set<SourceClass> selectImports = importSelector.selectImports(configurationClass.getAnnotationMetadata(), this.conditionEvaluator);
				// 解析导入类
				processImports(configurationClass, configurationClass.getSourceClass(), selectImports, true);
			}

		}
	}

	/** 封装ConfigurationClass与DeferredImportSelector的映射关系 */
	private static class DeferredImportSelectorHolder {
		private final ConfigurationClass configurationClass;
		private final DeferredImportSelector importSelector;

		public DeferredImportSelectorHolder(ConfigurationClass configClass, DeferredImportSelector selector) {
			this.configurationClass = configClass;
			this.importSelector = selector;
		}

		public ConfigurationClass getConfigurationClass() {
			return this.configurationClass;
		}

		public DeferredImportSelector getImportSelector() {
			return this.importSelector;
		}
	}
}

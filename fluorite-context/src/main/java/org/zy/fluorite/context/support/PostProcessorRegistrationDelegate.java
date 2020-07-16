package org.zy.fluorite.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanDefinitionRegistryPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanFactoryPostProcessor;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanPostProcessor;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.interfaces.PriorityOrdered;
import org.zy.fluorite.core.subject.OrderComparator;

/**
 * @DateTime 2020年6月18日 下午5:41:59;
 * @author zy(azurite-Y);
 * @Description
 */
public class PostProcessorRegistrationDelegate {

	/**
	 * 调用注册bean工厂后处理器
	 * @param beanFactory
	 */
	public static void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory,List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
		// 如果有，请先调用BeanDefinitionRegistryPostProcessors
		Set<String> processedBeans = new HashSet<>();

		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			List<BeanFactoryPostProcessor> regularPostProcessors = new LinkedList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new LinkedList<>();
			/**
			 * c.z.f.c.s.i.ConfigurationWarningsApplicationContextInitializer$ConfigurationWarningsPostProcessor.ConfigurationWarningsPostProcessor
			 * 检查根启动类的@ComponentScan注解的包扫描路径是否以“org”、“org.zy”、“org.zy.fluorite”开头，若包含则打印warn级别的日志信息输出有问题的包名且抛出参数异常
			 */
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor = (BeanDefinitionRegistryPostProcessor) postProcessor;
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				} else {
					regularPostProcessors.add(postProcessor);
				}
			}

			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			/**
			 * getBeanNamesForType：返回与给定类型（包括子类）匹配的bean的名称，包括FactoryBean创建的Bean
			 */
			String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class,true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) { // 如果bean类型匹配，则为true；如果不匹配或尚不能确定，则为false
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			// 排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			/**
			 * 调用实现PriorityOrdered接口的BeanDefinitionRegistryPostProcessors
			 */
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			/**
			 * 接下来，调用实现Ordered的BeanDefinitionRegistryPostProcessors
			 */
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);

			// 调用实现PriorityOrdered接口的BeanDefinitionRegistryPostProcessors
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);

			currentRegistryProcessors.clear();

			// 最后，调用剩下的所有BeanDefinitionRegistryPostProcessors实现，直到不再出现其他处理器为止.
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true,false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors); // currentRegistryProcessors：null
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			/**
			 * 现在，调用到目前为止处理的所有处理器的postprocessebeanfactory回调.
			 * ConfigurationWarningsApplicationContextInitializer$ConfigurationWarningsPostProcessor：空方法
			 * ConfigurationClassPostProcessor：
			 *  筛选出标注了@Configuration的类模型对象，为其使用CGLIB生成代理对象，使用代理对象的Class填充其模型对象的ClassName属性 
			 */
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);

			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		} else {
			// 调用使用上下文实例注册的工厂处理器.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// 不要在这里初始化factory beans：我们需要让所有常规bean都未初始化，以便让bean工厂后处理器应用于它们！
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// 在实现PriorityOrdered、Ordered和其他的BeanFactoryPostProcessors之间分离.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// 跳过-已在上面的第一阶段中处理
			} else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				// 优先的有序后处理器
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			} else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			} else {
				// 无序后处理器
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// 首先，调用实现PriorityOrdered的BeanFactoryPostProcessors.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// 接下来，调用实现Ordered的BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : orderedPostProcessorNames) { // 未执行
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// 最后，调用所有其他BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// 清除缓存的合并bean定义，因为后处理器可能修改了原始元数据，例如替换值中的占位符
		beanFactory.clearMetadataCache();
	}

	private static void invokeBeanDefinitionRegistryPostProcessors(Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {
		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	private static void invokeBeanFactoryPostProcessors(Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}
	
	/**
	 * 根据Ordered接口排序
	 * @param postProcessors
	 * @param beanFactory
	 */
	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		if (postProcessors.size() <= 1) {
			return;
		}
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}
	
	/**
	 * @param beanFactory
	 * @param abstractApplicationContext
	 */
	public static void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory,	AbstractApplicationContext applicationContext) {
		/**
		 * 获得BeanFactory的beanDefinitionNames集合中实现了BeanPostProcessor接口的BeanName
		 */
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// 在实现PriorityOrdered、Ordered和其他处理器的BeanPostProcessors之间分离.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			// 根据实现接口的类型进行归类筛选
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				// 根据BeanName获得指定Class类型实例对象
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
			} else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			} else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		/**
		 * 首先，向BeanFactory注册实现PriorityOrdered的BeanPostProcessors.
		 */
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		/**
		 * 接下来，向BeanFactory注册实现Ordered的BeanPostProcessors.
		 */
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		/**
		 * 现在，向BeanFactory注册所有常规BeanPostProcessors.
		 */
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// 注册后处理器，以便将内部bean检测为应用程序侦听器，并将其移动到处理器链的末端（用于获取代理等）.
//		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void registerBeanPostProcessors(	ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {
		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}
}

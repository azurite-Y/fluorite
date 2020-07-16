package org.zy.fluorite.context.support.initializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.factory.interfaces.BeanDefinitionRegistry;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.interfaces.processor.BeanDefinitionRegistryPostProcessor;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.context.interfaces.ApplicationContextInitializer;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;
import org.zy.fluorite.core.environment.Property;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.interfaces.PriorityOrdered;
import org.zy.fluorite.core.utils.AnnotationUtils;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年6月19日 下午1:58:15;
 * @author zy(azurite-Y);
 * @Description 包扫描路径路径控制
 */
public class ConfigurationWarningsApplicationContextInitializer	implements ApplicationContextInitializer<ConfigurableApplicationContext>,Ordered {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationWarningsApplicationContextInitializer.class);

	@Override
	public void initialize(ConfigurableApplicationContext context) {
		context.addBeanFactoryPostProcessor(
				new ConfigurationWarningsPostProcessor( getChecks(context) ));
	}

	protected List<Check> getChecks(ConfigurableApplicationContext context) {
		List<Check> list = new ArrayList<>();
		ConfigurableEnvironment environment = context.getEnvironment();
		String property = environment.getProperty(Property.PROBLEM_PACKAGES);
		if (Assert.hasText(property) && property.indexOf(",") != -1) {
			// 配置了不建议扫描的路径
			list.add( new ComponentScanPackageCheck(property.split(",")) );
		}
		list.add( new ComponentScanPackageCheck() );
		return list;
	}
	
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE - 10;
	}

	protected static final class ConfigurationWarningsPostProcessor implements PriorityOrdered, BeanDefinitionRegistryPostProcessor {
		private List<Check> checks;

		public ConfigurationWarningsPostProcessor(List<Check> checks) {
			this.checks = checks;
		}

		@Override
		public int getOrder() {
			return Ordered.LOWEST_PRECEDENCE - 1;
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		}

		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
			for (Check check : this.checks) {
				String message = check.getWarning(registry);
				if (Assert.hasText(message)) {
					warn(message);
				}
			}
		}

		private void warn(String message) {
			if (logger.isWarnEnabled()) {
				logger.warn(String.format("%n%n** WARNING ** : %s%n%n", message));
			}
		}

	}

	/**
	 * A single check that can be applied.
	 */
	@FunctionalInterface
	protected interface Check {
		/**
		 * 如果检查失败，则返回警告；如果没有问题，则返回null
		 */
		String getWarning(BeanDefinitionRegistry registry);

	}

	/**
	 * 检查包扫描路径是否合规
	 */
	protected static class ComponentScanPackageCheck implements Check {
		private final Set<String> PROBLEM_PACKAGES = new HashSet<>();

		public ComponentScanPackageCheck(String... packages) {
			if (packages != null && packages.length > 0) {
				for (String str : packages) {
					PROBLEM_PACKAGES.add(str);
				}
			} else {
				PROBLEM_PACKAGES.add("org");
				PROBLEM_PACKAGES.add("org.zy");
				PROBLEM_PACKAGES.add("org.zy.fluorite");
			}
		}

		@Override
		public String getWarning(BeanDefinitionRegistry registry) {
			// 获得包扫描路径
			List<String> scannedPackages = getComponentScanningPackages(registry);
			// 检查路径，返回不合规的路径
			List<String> problematicPackages = getProblematicPackages(scannedPackages);
			if (problematicPackages.isEmpty()) {
				return null;
			}
			return "应用程序上下文无法启动，不合规的包扫描路径，by basePackages：" + problematicPackages;
		}

		/**
		 * 获得包扫描路径
		 * 
		 * @param registry
		 * @return
		 */
		protected List<String> getComponentScanningPackages(BeanDefinitionRegistry registry) {
			List<String> packages = new ArrayList<>();
			// 获得所有注册BeanDefinition的名称集合，包括根启动类
			List<String> names = registry.getBeanDefinitionNames();
			for (String name : names) {
				BeanDefinition definition = registry.getBeanDefinition(name);
				addComponentScanningPackages(packages, definition.getAnnotationMetadata());
			}
			return packages;
		}

		private void addComponentScanningPackages(List<String> packages, AnnotationMetadata metadata) {
			packages.addAll(AnnotationUtils.findComponentScanFromAnnotation(metadata));
		}

		/**
		 * 检查路径，返回不合规的路径
		 * 
		 * @param packages
		 * @param values
		 */
		private List<String> getProblematicPackages(List<String> scannedPackages) {
			List<String> problematicPackages = new ArrayList<>();
			for (String scannedPackage : scannedPackages) {
				if (isProblematicPackage(scannedPackage)) {
					problematicPackages.add(scannedPackage);
				}
			}
			return problematicPackages;
		}

		private boolean isProblematicPackage(String scannedPackage) {
			if (scannedPackage == null || scannedPackage.isEmpty()) {
				return true;
			}
			return PROBLEM_PACKAGES.contains(scannedPackage);
		}
	}
}

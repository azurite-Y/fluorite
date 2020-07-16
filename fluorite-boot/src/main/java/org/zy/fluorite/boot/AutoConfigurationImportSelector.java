package org.zy.fluorite.boot;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.support.SourceClass;
import org.zy.fluorite.context.annotation.ConditionEvaluator;
import org.zy.fluorite.context.annotation.interfaces.DeferredImportSelector;
import org.zy.fluorite.core.environment.FactoriesProperty;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.io.MetaFileLoader;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年6月29日 下午3:19:05;
 * @author zy(azurite-Y);
 * @Description 延迟导入选择器以处理自动配置
 */
public class AutoConfigurationImportSelector  implements DeferredImportSelector, Ordered{
	private static final Logger logger = LoggerFactory.getLogger(AutoConfigurationImportSelector.class);
	
	private ConfigurableListableBeanFactory beanFactory;

	private Environment environment;
	
	@Override
	public Set<SourceClass> selectImports(AnnotationMetadata annotationMetadata, ConditionEvaluator conditionEvaluator) {
		Group group = getImportGroup();
		return  group.selectImports(annotationMetadata,conditionEvaluator);
	}

	@Override
	public Group getImportGroup() {
		DebugUtils.log(logger, "加载自动配置，by：AutoConfigurationGroup.class");
		return new AutoConfigurationGroup();
	}
	
	@Override
	public void invokeAwareMethods(Environment environment, ConfigurableListableBeanFactory registry) {
		setBeanFactory(beanFactory);
		setEnvironment(environment);
	}

	public ConfigurableListableBeanFactory getBeanFactory() {
		return beanFactory;
	}
	public void setBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	public Environment getEnvironment() {
		return environment;
	}
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1;
	}
	
	private class AutoConfigurationGroup implements DeferredImportSelector.Group {
		
		public AutoConfigurationGroup() {}
		
		@Override
		public Set<SourceClass> selectImports(AnnotationMetadata annotationMetadata , ConditionEvaluator conditionEvaluator) {
			Set<SourceClass> contain = new LinkedHashSet<>();
			List<String> loadFactories = MetaFileLoader.loadFactories("fluorite-autoconfigure", "fluorite.factories", FactoriesProperty.ENABLE_AUTOCONFIGURATION);
			for (String clzName : loadFactories) {
				DebugUtils.log(logger, "从'fluorite.factories'文件中读取到的[EnableAutoConfiguration]预置项："+clzName);
				
				Class<?> forName = ReflectionUtils.forName(clzName);
				if (forName == null) {
					logger.error("从fluorite-autoconfigure模块下的‘META-INFO/fluorite.factories文件中载入预设的自动配置类失败。"
							+ "by key：" +FactoriesProperty.ENABLE_AUTOCONFIGURATION+ " value：" + clzName);
				}
				SourceClass sourceClass = new SourceClass(forName);
				// 条件评估
//				if (!conditionEvaluator.shouldSkip(sourceClass.getAnnotationMetadata(), ConfigurationPhase.PARSE_CONFIGURATION)) {
//				}
				contain.add(sourceClass);
			}
			return contain;
		}
		
	}
}

package org.zy.fluorite.autoconfigure.transaction;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.support.SourceClass;
import org.zy.fluorite.context.annotation.ConditionEvaluator;
import org.zy.fluorite.context.annotation.interfaces.ImportSelector;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @DateTime 2021年9月16日;
 * @author zy(azurite-Y);
 * @Description
 */
@Deprecated
public class TransactionManagementConfigurationSelector implements ImportSelector {
	private static final Logger logger = LoggerFactory.getLogger(TransactionManagementConfigurationSelector.class);
	
	private ConfigurableListableBeanFactory beanFactory;

	private Environment environment;
	
	@Override
	public Set<SourceClass> selectImports(AnnotationMetadata annotationMetadata, ConditionEvaluator conditionEvaluator) {
		Set<SourceClass> contain = new LinkedHashSet<>();
		// 在此注册事务设施配置类
		contain.add(new SourceClass(TransactionAutoConfiguration.class));
		contain.add(new SourceClass(DataSourceTransactionManagerAutoConfiguration.class));
		DebugUtils.logFromTransaction(logger, "注册事务设施配置类：" + contain);
		return contain;
	}

	@Override
	public void invokeAwareMethods(Environment environment, ConfigurableListableBeanFactory beanFactory) {
		this.setBeanFactory(beanFactory);
		this.setEnvironment(environment);
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

}

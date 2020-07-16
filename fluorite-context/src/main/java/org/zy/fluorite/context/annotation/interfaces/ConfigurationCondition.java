package org.zy.fluorite.context.annotation.interfaces;

/**
 * @DateTime 2020年6月20日 下午1:27:36;
 * @author zy(azurite-Y);
 * @Description 与@Configuration一起使用时提供更细粒度控制的条件。允许某些条件在基于配置阶段匹配时进行调整。
 */
public interface ConfigurationCondition extends Condition {
	ConfigurationPhase getConfigurationPhase();
	
	enum ConfigurationPhase {
		/**
		 * 应将条件计算为正在分析@Configuration类。
		 * 如果此时条件不匹配，则不会添加@Configuration类
		 */
		PARSE_CONFIGURATION,

		/**
		 * 评估配置类中的bean注册时的条件。
		 * 这不会阻止添加配置类，但如果条件不匹配（由Condition的matches方法定义），则不注册此BeanDefinition
		 */
		REGISTER_BEAN
	}
}

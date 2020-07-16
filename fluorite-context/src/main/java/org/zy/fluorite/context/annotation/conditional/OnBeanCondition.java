package org.zy.fluorite.context.annotation.conditional;

import java.util.List;

import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.context.annotation.interfaces.ConditionContext;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年6月30日 下午12:58:54;
 * @author zy(azurite-Y);
 * @Description
 */
public class OnBeanCondition extends FluoriteConditional  {
	public OnBeanCondition() {}

	@Override
	public ConfigurationPhase getConfigurationPhase() {
		return ConfigurationPhase.REGISTER_BEAN;
	}
	
	@Override
	public boolean matcher(ConditionContext context, AnnotationAttributes attributes) {
		boolean flag = false;
		List<ConditionalOnBean> conditionalOnBeans = attributes.getAnnotationList(ConditionalOnBean.class);
		if (conditionalOnBeans != null) {
			for (ConditionalOnBean conditionalOnBean : conditionalOnBeans) {
				flag = matcher(conditionalOnBean, context);
				if (!flag) {return false;} // 一但条件被否决就直接返回结果
			}
		}
		
		List<ConditionalOnMissingBean> conditionalOnMissingBeans = attributes.getAnnotationList(ConditionalOnMissingBean.class);
		if (conditionalOnMissingBeans != null) {
			for (ConditionalOnMissingBean conditionalOnMissingBean : conditionalOnMissingBeans) {
				flag = matcher(conditionalOnMissingBean, context);
				if (!flag) {return false;} // 一但条件被否决就直接返回结果
			}
		}

		List<ConditionalOnSingleCandidate> conditionalOnSingleCandidates = attributes.getAnnotationList(ConditionalOnSingleCandidate.class);
		if (conditionalOnSingleCandidates != null) {
			for (ConditionalOnSingleCandidate conditionalOnSingleCandidate : conditionalOnSingleCandidates) {
				flag = matcher(conditionalOnSingleCandidate, context);
				if (!flag) {return false;} // 一但条件被否决就直接返回结果
			}
		}
		if (flag) {return true;} // 经过以上决断都未被否决则代表条件匹配，至少在此Condition实现的判断结果是这样的
		
		// 标识到此为false则代表只标注了@Conditional注解引入当前实现类而为标注其他条件注解
		// 或标注的是自定义的条件注解但错误的引入了当前实现
		throw new NotFoundConditionException("无法找到对应的条件注解解析方法，by source："+attributes.getElement().toString());
	}

	protected boolean matcher(ConditionalOnBean conditional, ConditionContext context) {
		if (!super.matcher(conditional.value(), context) || !super.matcher(conditional.type(), context) ||
				!super.matcher(conditional.name(), conditional.annotation(), context) ) {
			return false;
		}
		return true;
	}

	protected boolean matcher(ConditionalOnMissingBean conditional, ConditionContext context) {
		if (super.matcher(conditional.value(), context) || super.matcher(conditional.type(), context) || 
				super.matcher(conditional.name(), conditional.annotation(), context) ) {
			return false;
		}
		return true;
	}

	protected boolean matcher(ConditionalOnSingleCandidate conditional, ConditionContext context) {
		String[] beanNamesForType = null;
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

		Class<?>[] values = conditional.value();
		for (Class<?> clz : values) {
			beanNamesForType = beanFactory.getBeanNamesForType(clz);
			if (! doMatcherBeanSingletion(beanNamesForType , beanFactory) ) {return false;}
		}

		String[] types = conditional.type();
		Class<?> forName;
		for (String clzName : types) {
			forName = ReflectionUtils.forName(clzName);
			beanNamesForType = beanFactory.getBeanNamesForType(forName);
			if (! doMatcherBeanSingletion(beanNamesForType , beanFactory) ) {return false;}
		}
		return true;
	}

	/**
	 * 检查指定的beanName集合所代表的Bean是否都是单例的。
	 * @param beanNames
	 * @param beanFactory
	 * @return
	 */
	private boolean doMatcherBeanSingletion(String[] beanNames , ConfigurableListableBeanFactory beanFactory) {
		RootBeanDefinition beanDefinition = null;
		if (beanNames.length == 0) { // 指定类型的Bean未在容器中
			return false;
		} else if (beanNames.length == 1) { // 指定类型的Bean存在于容器中，但只有一个则返回true
			beanDefinition = beanFactory.getBeanDefinition(beanNames[0]);
			return true;
		} else {
			for (String beanName : beanNames) { // 指定类型的Bean有多个存在于容器中，若之中之一标注了 @Primary 则返回true
				beanDefinition = beanFactory.getBeanDefinition(beanName);
				if (beanDefinition.isPrimary()) {
					return true;
				}
			}
			return false;
		}
	}
}
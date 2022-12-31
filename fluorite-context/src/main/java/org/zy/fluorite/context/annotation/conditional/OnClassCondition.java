package org.zy.fluorite.context.annotation.conditional;

import java.util.List;

import org.zy.fluorite.context.annotation.interfaces.ConditionContext;
import org.zy.fluorite.context.annotation.interfaces.ConfigurationCondition;
import org.zy.fluorite.core.subject.AnnotationAttributes;

/**
 * @DateTime 2020年6月30日 下午1:01:05;
 * @author zy(azurite-Y);
 * @Description
 */
public class OnClassCondition implements ConfigurationCondition {
	public OnClassCondition() {	}

	@Override
	public ConfigurationPhase getConfigurationPhase() {
		return ConfigurationPhase.PARSE_CONFIGURATION;
	}
	
	@Override
	public boolean matcher(ConditionContext context, AnnotationAttributes attributes) {
		boolean flag = false;
		List<ConditionalOnClass> conditionalOnClasss = attributes.getAnnotationList(ConditionalOnClass.class);
		if (conditionalOnClasss != null) {
			for (ConditionalOnClass conditionalOnClass : conditionalOnClasss) {
				flag = matcher(conditionalOnClass, context);
			}
			if (!flag) {return false;} // 一但条件被否决就直接返回结果
		}
		List<ConditionalOnMissingClass> conditionalOnMissingClasss = attributes.getAnnotationList(ConditionalOnMissingClass.class);
		if (conditionalOnMissingClasss != null) {
			for (ConditionalOnMissingClass conditionalOnMissingClass : conditionalOnMissingClasss) {
				return matcher(conditionalOnMissingClass, context);
			}
			if (!flag) {return false;} // 一但条件被否决就直接返回结果
		}
		if (flag) {return true;} // 经过以上决断都未被否决则代表条件匹配，至少在此Condition实现判断结果是这样的
		
		// 标识到此为false则代表只标注了@Conditional注解引入当前实现类而为标注其他条件注解。
		// 或标注的是自定义的条件注解但错误的引入了当前实现
		throw new NotFoundConditionException("无法找到对应的条件注解解析方法，by source：" + attributes.getElement().toString());
	}

	protected boolean matcher(ConditionalOnClass conditional, ConditionContext context) {
		return matcherType(conditional.type() , context);
	}

	protected boolean matcher(ConditionalOnMissingClass conditional, ConditionContext context) {
		return !matcherType(conditional.type() , context);
	}

	
	/**
	 * 若不能通过给定的全称类名获得Class对象，则返回false
	 * @param types
	 * @param context
	 * @return 条件匹配或匹配内容为空集则返回true
	 */
	private boolean matcherType(String[] types , ConditionContext context) {
		if (types.length == 0) {return true;}
		
		for (String clzName : types) {
			try {
				Class.forName(clzName, false, ClassLoader.getSystemClassLoader());
//				Class.forName(clzName);
			} catch (Throwable e) {
				return false;
			}
		}
		return true;
	}
}

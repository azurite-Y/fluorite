package org.zy.fluorite.context.annotation.conditional;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.context.annotation.interfaces.ConditionContext;
import org.zy.fluorite.context.annotation.interfaces.ConfigurationCondition;
import org.zy.fluorite.core.environment.interfaces.Environment;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @DateTime 2020年6月30日 下午1:02:08;
 * @author zy(azurite-Y);
 * @Description
 */
public class OnPropertyCondition implements ConfigurationCondition {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	public OnPropertyCondition() {}

	@Override
	public boolean matcher(ConditionContext context, AnnotationAttributes attributes) {
		Environment environment = context.getEnvironment();

		List<ConditionalOnProperty> conditionalOnPropertys = attributes.getAnnotationList(ConditionalOnProperty.class);
		if (conditionalOnPropertys == null) {
			return false;
		}
		for (ConditionalOnProperty conditionalOnProperty : conditionalOnPropertys) {
			String[] values = conditionalOnProperty.value();
			Assert.notNull(values,"@ConditionalOnProperty指定的属性名数组不能为空集");
			
			String prefix = conditionalOnProperty.prefix();
			String havingValue = conditionalOnProperty.havingValue();
			boolean matchIfMissing = conditionalOnProperty.matchIfMissing();
			int prefixLen = prefix.length();

			StringBuilder builder = new StringBuilder();
			builder.append(prefix);
			for (String value : values) {
				Assert.hasText(value,"@ConditionalOnProperty指定的属性名不能为空串");
				if (builder.length() > prefixLen)
					builder.delete(prefixLen, builder.length());

				builder.append(value);

				String property = environment.getProperty(builder.toString());
				if (property == null) { // 若matchIfMissing为true则跳过此属性
					if (matchIfMissing) {
						DebugUtils.log(logger, "@ConditionalOnProperty-忽略未定义的属性 ，by key："+ builder.toString() +" 源头："+attributes.getDisplayName());
						continue;
					} else {
						DebugUtils.log(logger, "@ConditionalOnProperty-条件不成立 [理由：未定义此属性] ，by key："+ builder.toString() +" 源头："+attributes.getDisplayName());
						return false;
					}
				} else if (!havingValue.isEmpty() && !havingValue.equals(property)) { // 与预期不符
					DebugUtils.log(logger, "@ConditionalOnProperty-条件不成立 [理由：不符合预期属性值的] ，by key："+ builder.toString() +"预期属性值："+havingValue+" 源头："+attributes.getDisplayName());
					return false;
				}
				DebugUtils.log(logger, "@ConditionalOnProperty-条件成立 ，by key："+ builder.toString() +" 源头："+attributes.getDisplayName());
			}
		}
		return true;
	}

	@Override
	public ConfigurationPhase getConfigurationPhase() {
		return ConfigurationPhase.PARSE_CONFIGURATION;
	}

}

package org.zy.fluorite.context.annotation.conditional;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.context.annotation.interfaces.ConditionContext;
import org.zy.fluorite.context.annotation.interfaces.ConfigurationCondition;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @DateTime 2020年6月30日 下午1:01:51;
 * @author zy(azurite-Y);
 * @Description
 */
public class OnResourceCondition implements ConfigurationCondition {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	public OnResourceCondition() {}

	@Override
	public boolean matcher(ConditionContext context, AnnotationAttributes attributes) {
		List<ConditionalOnResource> conditionalOnResources = attributes.getAnnotationList(ConditionalOnResource.class);
		if (conditionalOnResources != null) {
			for (ConditionalOnResource conditionalOnResource : conditionalOnResources) {
				String[] resources = conditionalOnResource.resources();
				URL url = null;
				for (String resource : resources) {
					url = ClassLoader.getSystemResource(resource);
					if (url == null) {
						DebugUtils.log(logger, "@ConditionalOnResource-条件不成立 [理由：指定路径的资源不存在] ，by path："+ resource +" 源头："+attributes.getDisplayName());
						return false;
					}
					DebugUtils.log(logger, "@ConditionalOnResource-条件成立 ，by path："+ resource +" 源头："+attributes.getDisplayName());
				}
			}
		}
		return true;
	}

	@Override
	public ConfigurationPhase getConfigurationPhase() {
		return ConfigurationPhase.PARSE_CONFIGURATION;
	}

}

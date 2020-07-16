package org.zy.fluorite.beans.interfaces;

import org.zy.fluorite.beans.factory.support.DependencyDescriptor;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月13日 下午4:19:13;
 * @Description
 */
public interface AutowireCandidateResolver {
	
	/**
	 * 确定给定的bean定义是否符合给定依赖项的自动连线候选。
	 */
	default boolean isAutowireCandidate(BeanDefinition bd, DependencyDescriptor descriptor) {
		return bd.isAutowireCandidate();
	}

	/**
	 * 确定是否有效地需要给定的描述符。默认实现检查需要DependencyDescriptor.isRequired().
	 */
	default boolean isRequired(DependencyDescriptor descriptor) {
		return descriptor.isRequired();
	}

	/**
	 * 确定给定的描述符是否声明了类型之外的限定符（通常但不一定是特定类型的注释）。默认实现返回false。
	 */
	default boolean hasQualifier(DependencyDescriptor descriptor) {
		return false;
	}

	/**
	 * 确定是否为给定依赖项建议默认值。默认实现只返回空值。
	 */
	default Object getSuggestedValue(DependencyDescriptor descriptor) {
		return null;
	}

	/**
	 * 如果注入点需要，为实际依赖目标的延迟解析构建代理。默认实现只返回空值。
	 */
	default Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName) {
		return null;
	}
}

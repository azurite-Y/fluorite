package org.zy.fluorite.aop.support;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.DynamicIntroductionAdvice;
import org.zy.fluorite.aop.interfaces.IntroductionAdvisor;
import org.zy.fluorite.aop.interfaces.IntroductionInfo;
import org.zy.fluorite.aop.interfaces.function.ClassFilter;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;

/**
 * @DateTime 2020年7月4日 下午3:47:40;
 * @author zy(azurite-Y);
 * @Description IntroductionAdvisor的简单实现，默认情况下应用于任何类。
 */
@SuppressWarnings("serial")
public class DefaultIntroductionAdvisor implements IntroductionAdvisor, ClassFilter, Ordered, Serializable {
	private final Advice advice;

	private final Set<Class<?>> interfaces = new LinkedHashSet<>();

	private int order = Ordered.LOWEST_PRECEDENCE;

	public DefaultIntroductionAdvisor(Advice advice) {
		this(advice, (advice instanceof IntroductionInfo ? (IntroductionInfo) advice : null));
	}

	/**
	 * 为给定的建议创建DefaultIntroductionAdvisor
	 * @param advice - 适用的建议
	 * @param introductionInfo - 描述要引入的接口的介绍信息（可以为空）
	 */
	public DefaultIntroductionAdvisor(Advice advice, IntroductionInfo introductionInfo) {
		Assert.notNull(advice, "Advice must not be null");
		this.advice = advice;
		if (introductionInfo != null) {
			Class<?>[] introducedInterfaces = introductionInfo.getInterfaces();
			Assert.notNull(introducedInterfaces,"IntroductionAdviceSupport 未实现任何接口");
			for (Class<?> ifc : introducedInterfaces) {
				addInterface(ifc);
			}
		}
	}

	/**
	 * 为给定的建议创建DefaultIntroductionAdvisor
	 * @param advice - 要使用的Advice
	 * @param ifc - 要引入的接口
	 */
	public DefaultIntroductionAdvisor(DynamicIntroductionAdvice advice, Class<?> ifc) {
		Assert.notNull(advice, "Advice不能为null");
		this.advice = advice;
		addInterface(ifc);
	}


	/** 将指定的接口添加到要引入的接口列表中 */
	public void addInterface(Class<?> ifc) {
		Assert.notNull(ifc, "指定的接口不能为null");
		Assert.isTrue(ifc.isInterface(), "指定的Class对象不是一个接口");
		this.interfaces.add(ifc);
	}

	@Override
	public Class<?>[] getInterfaces() {
		return ClassUtils.toClassArray(this.interfaces);
	}

	@Override
	public void validateInterfaces() throws IllegalArgumentException {
		for (Class<?> ifc : this.interfaces) {
			if (this.advice instanceof DynamicIntroductionAdvice &&
					!((DynamicIntroductionAdvice) this.advice).implementsInterface(ifc)) {
				throw new IllegalArgumentException("DynamicIntroductionAdvice对象 [" + this.advice + "] " +
						"未实现指定的引入接口：" + ifc.getName());
			}
		}
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public Advice getAdvice() {
		return this.advice;
	}

	@Override
	public boolean matches(Class<?> clazz) {
		return true;
	}

	@Override
	public ClassFilter getClassFilter() {
		return this;
	}
}

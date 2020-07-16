package org.zy.fluorite.aop.interfaces;

import org.zy.fluorite.aop.interfaces.function.ClassFilter;

/**
 * @DateTime 2020年7月4日 下午3:06:07;
 * @author zy(azurite-Y);
 * @Description 执行一个或多个AOP介绍的顾问的超级接口。此接口不能直接实现；子接口必须提供实现引入的建议类型。
 */
public interface IntroductionAdvisor extends Advisor, IntroductionInfo {
	/** 返回确定此介绍应应用于哪些目标类的筛选器 */
	ClassFilter getClassFilter();

	/** 建议的接口是否可以通过引入建议来实现？在添加IntroductionAdvisor之前调用 */
	void validateInterfaces() throws IllegalArgumentException;

}

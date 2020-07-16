package org.zy.fluorite.aop.interfaces;

import org.zy.fluorite.aop.exception.AopConfigException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午2:05:02;
 * @Description
 */
public interface Advised extends TargetClassAware  {
	/** 返回建议的配置是否冻结，在这种情况下，不能进行通知更改 */
	boolean isFrozen();

	/** 判断是否代理完整的目标类而不是指定的接口 */
	boolean isProxyTargetClass();

	/**
	 * 返回AOP代理的的代理接口。
	 * 将不包括目标类，该类也可能被代理
	 */
	Class<?>[] getProxiedInterfaces();

	/** 确定给定接口是否被代理 */
	boolean isInterfaceProxied(Class<?> intf);

	/**
	 * 更改此建议对象使用的TargetSource。
	 * 仅当配置未冻结时才有效
	 */
	void setTargetSource(TargetSource targetSource);

	/** 获得被代理的对象 */
	TargetSource getTargetSource();

	/**
	 * 设置AOP框架是否应将代理作为ThreadLocal公开，以便通过AopContext类进行检索。
	 * 如果建议对象需要在应用通知的情况下对其自身调用方法，则可能需要公开代理。
	 * 否则，如果advised对象对此调用方法，则不会应用通知。
	 * 默认值为false，以获得最佳性能
	 */
	void setExposeProxy(boolean exposeProxy);

	/** 返回工厂是否应将代理公开到ThreadLocal */
	boolean isExposeProxy();

	/**
	 * 设置是否预先筛选此代理配置，使其仅包含适用的顾问（匹配此代理的目标类）。默认值为“false”。
	 * 如果已经对advisor进行了预过滤，则将其设置为“true”，
	 * 这意味着在为代理调用构建实际的advisor链时可以跳过ClassFilter检查
	 */
	void setPreFiltered(boolean preFiltered);

	/** 返回此代理配置是否已预先筛选，以便仅包含适用的顾问（匹配此代理的目标类）*/
	boolean isPreFiltered();

	/** 返回适用于此代理的顾问列表（从不为空）*/
	Advisor[] getAdvisors();

	/** 在advisor链的末端添加一个advisor */
	void addAdvisor(Advisor advisor) throws AopConfigException;

	/** 在advisor链的指定位置添加Advisor */
	void addAdvisor(int pos, Advisor advisor) throws AopConfigException;

	/**
	 * 移除给定的Advisor
	 */
	boolean removeAdvisor(Advisor advisor);

	/**
	 * 移除给定位置的Advisor
	 */
	void removeAdvisor(int index) throws AopConfigException;

	/** 获得给定Advisor的下标 */
	int indexOf(Advisor advisor);

	/** Advisor替换 */
	boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException;

	/** 将给定的Advice添加到Advice链的尾部 */
	void addAdvice(Advice advice) throws AopConfigException;

	void addAdvice(int pos, Advice advice) throws AopConfigException;

	boolean removeAdvice(Advice advice);

	int indexOf(Advice advice);

	/** 由于toString（）通常会被委托给目标，因此它将返回AOP代理的等效值 */
	String toProxyConfigString();
}

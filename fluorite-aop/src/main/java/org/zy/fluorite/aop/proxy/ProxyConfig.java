package org.zy.fluorite.aop.proxy;

import java.io.Serializable;

import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月4日 下午1:15:34;
 * @author zy(azurite-Y);
 * @Description 用于创建代理的配置的便利超类，以确保所有代理创建者具有一致的属性
 */
@SuppressWarnings("serial")
public class ProxyConfig implements Serializable {

	/** 标记是否直接对目标类进行代理，而不是通过接口产生代理 */
	boolean proxyTargetClass = false;

	/**
	 * 标记是否对代理进行优化。启动优化通常意味着在代理对象被创建后，增强的修改将不会生效，因此默认值为false。
	 * 如果将optimize设置为true，那么在生成代理对象之后，如果对代理配置进行了修改，已经创建的代理对象也不会获取修改之后的代理配置
	 * 如果exposeProxy设置为true，即使optimize为true也会被忽略。
	 */
	boolean optimize = false;

	/**
	 * 标记是否需要阻止通过该配置创建的代理对象转换为Advised类型。
	 * 默认值为false，表示代理对象可以被转换为Advised类型 
	 */
	boolean opaque = false;

	/**
	 * 标记代理对象是否应该被aop框架通过AopContext以ThreadLocal的形式暴露出去。
	 * 当一个代理对象需要调用它自己的另外一个代理方法时，这个属性将非常有用。默认是是false，以避免不必要的拦截。 
	 */
	boolean exposeProxy = false;

	/**
	 * 标记该配置是否需要被冻结，如果被冻结，将不可以修改增强的配置。
	 * 当我们不希望调用方修改转换成Advised对象之后的代理对象时，这个配置将非常有用。
	 */
	private boolean frozen = false;

	/**
	 * 设置是否直接代理目标类，而不只是代理特定接口。默认值为“false”。
	 * 将其设置为“true”以强制对TargetSource的公开目标类执行代理。如果目标类是接口，则将为给定接口创建一个JDK代理。如果目标类是任何其他类，则将为给定类创建一个CGLIB代理。
	 * 注意：根据具体代理工厂的配置，如果没有指定接口（并且没有激活接口自动检测），也将应用proxy target类行为。
	 */
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}

	/**
	 * 判断是否直接代理目标类以及任何接口
	 */
	public boolean isProxyTargetClass() {
		return this.proxyTargetClass;
	}

	/**
	 * 设置代理是否应执行积极的优化。“激进优化”的确切含义在代理之间会有所不同，但通常会有一些折衷。默认值为“false”。
	 * 例如，优化通常意味着在创建代理之后，通知更改不会生效。因此，默认情况下禁用优化。
	 * 如果其他设置排除优化，则优化值“true”可能会被忽略：例如，如果“exposeProxy”设置为“true”，并且与优化不兼容。
	 */
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	/**
	 * 判断代理是否应执行积极的优化。
	 */
	public boolean isOptimize() {
		return this.optimize;
	}

	/**
	 * 设置是否应防止将此配置创建的代理强制转换为建议查询代理状态。 默认值为“false”，这意味着任何AOP代理都可以强制转换为advicted
	 */
	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	/**
	 * 判断是否应阻止此配置创建的代理被强制转换为通知代理
	 */
	public boolean isOpaque() {
		return this.opaque;
	}

	/**
	 * 设置AOP框架是否应将代理作为ThreadLocal公开，以便通过AopContext类进行检索。如果建议的对象需要对其自身调用另一个adviced方法，这很有用。（如果使用此选项，则不会通知其位置）。
	 * 默认值为“false”，以避免不必要的额外拦截。这意味着不能保证AopContext访问将在建议对象的任何方法中一致地工作
	 */
	public void setExposeProxy(boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
	}

	/**
	 * 判断AOP代理是否将为每次调用公开AOP代理
	 */
	public boolean isExposeProxy() {
		return this.exposeProxy;
	}

	/**
	 * 设置是否应冻结此配置。 当配置被冻结时，不能进行任何建议更改。
	 * 这对于优化非常有用，当我们不希望调用者在强制转换为adviced后能够操纵配置时也很有用。
	 */
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	/**
	 * 配置是否冻结，不能进行任何建议更改
	 */
	public boolean isFrozen() {
		return this.frozen;
	}

	/**
	 * 从其他配置对象复制配置
	 */
	public void copyFrom(ProxyConfig other) {
		Assert.notNull(other, "ProxyConfig的复制对象不能为null");
		this.proxyTargetClass = other.isProxyTargetClass();
		this.optimize = other.optimize;
		this.exposeProxy = other.exposeProxy;
		this.frozen = other.frozen;
		this.opaque = other.opaque;
	}
}

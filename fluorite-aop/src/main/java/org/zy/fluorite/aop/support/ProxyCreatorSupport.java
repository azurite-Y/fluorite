package org.zy.fluorite.aop.support;

import java.util.LinkedList;
import java.util.List;

import org.zy.fluorite.aop.interfaces.AdvisedSupportListener;
import org.zy.fluorite.aop.interfaces.AopProxy;
import org.zy.fluorite.aop.interfaces.AopProxyFactory;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月4日 下午4:36:56;
 * @author zy(azurite-Y);
 * @Description 代理的基类工厂。提供访问可配置的AopProxyFactory的方便
 */
@SuppressWarnings("serial")
public class ProxyCreatorSupport extends AdvisedSupport {

	private AopProxyFactory aopProxyFactory;

	private final List<AdvisedSupportListener> listeners = new LinkedList<>();

	/** 在创建第一个AOP代理时设置为true */
	private boolean active = false;

	public ProxyCreatorSupport() {
		this.aopProxyFactory = new DefaultAopProxyFactory();
	}

	public ProxyCreatorSupport(AopProxyFactory aopProxyFactory) {
		Assert.notNull(aopProxyFactory, "AopProxyFactory不能为null");
		this.aopProxyFactory = aopProxyFactory;
	}

	/**
	 * 定制AopProxyFactory，允许在不改变核心框架的情况下加入不同的策略。
	 * 默认值是DefaultAopProxyFactory，根据需求使用动态JDK代理或CGLIB代理
	 */
	public void setAopProxyFactory(AopProxyFactory aopProxyFactory) {
		Assert.notNull(aopProxyFactory, "AopProxyFactory不能为null");
		this.aopProxyFactory = aopProxyFactory;
	}

	/** 获得要使用的AopProxyFactory对象 */
	public AopProxyFactory getAopProxyFactory() {
		return this.aopProxyFactory;
	}

	/** 将给定的AdvisedSupportListener添加到此代理配置中 */
	public void addListener(AdvisedSupportListener listener) {
		Assert.notNull(listener, "AdvisedSupportListener must not be null");
		this.listeners.add(listener);
	}

	public void removeListener(AdvisedSupportListener listener) {
		Assert.notNull(listener, "AdvisedSupportListener不能为null");
		this.listeners.remove(listener);
	}

	/** 子类应该调用此函数以获取新的AOP代理 */
	protected final synchronized AopProxy createAopProxy() {
		if (!this.active) {
			activate();
		}
		return getAopProxyFactory().createAopProxy(this);
	}

	/** 激活此代理配置 */
	private void activate() {
		this.active = true;
		for (AdvisedSupportListener listener : this.listeners) {
			listener.activated(this);
		}
	}

	/** 将通知更改事件传播到所有AdvisedSupportListener。*/
	@Override
	protected void adviceChanged() {
		super.adviceChanged();
		synchronized (this) {
			if (this.active) {
				for (AdvisedSupportListener listener : this.listeners) {
					listener.adviceChanged(this);
				}
			}
		}
	}

	/** 子类可以调用此函数来检查是否有任何AOP代理已被创建 */
	protected final synchronized boolean isActive() {
		return this.active;
	}
}

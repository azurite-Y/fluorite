package org.zy.fluorite.aop.interfaces;

import org.zy.fluorite.aop.support.AdvisedSupport;

/**
 * @DateTime 2020年7月4日 下午4:44:55;
 * @author zy(azurite-Y);
 * @Description 要注册为ProxyCreator的侦听器对象，允许接收有关激活和通知更改的回调
 */
public interface AdvisedSupportListener {
	/** 在创建第一个代理时调用 */
	void activated(AdvisedSupport advised);

	/** 在创建代理后更改通知时调用 */
	void adviceChanged(AdvisedSupport advised);
}

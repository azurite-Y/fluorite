package org.zy.fluorite.aop.aspectj.interfaces;

import org.zy.fluorite.aop.interfaces.PointcutAdvisor;

/**
 * @DateTime 2020年7月6日 下午4:45:05;
 * @author zy(azurite-Y);
 * @Description 封装可能具有延迟初始化策略的切面
 */
public interface InstantiationModelAwarePointcutAdvisor extends PointcutAdvisor {
	
	/** 判断此Avisor是否是懒加载的 */
	boolean isLazy();

	/** 判断此advisor 是否已实例化其advice */
	boolean isAdviceInstantiated();
}

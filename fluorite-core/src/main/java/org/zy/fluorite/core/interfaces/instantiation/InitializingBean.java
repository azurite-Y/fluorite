package org.zy.fluorite.core.interfaces.instantiation;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午1:29:08;
 * @Description 初始化方法的回调接口
 */
public interface InitializingBean {
	void afterPropertiesSet() throws Exception;
}

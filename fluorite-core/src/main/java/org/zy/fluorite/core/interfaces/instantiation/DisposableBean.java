package org.zy.fluorite.core.interfaces.instantiation;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午1:29:17;
 * @Description 销毁方法的回调接口
 */
public interface DisposableBean {
	void destroy() throws Exception;
}

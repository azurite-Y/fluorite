package org.zy.fluorite.core.interfaces;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午12:03:10;
 * @Description 将由承载配置源对象的bean元数据元素实现的接口。
 */
public interface BeanMetadataElement {
	
	/**
	 * 返回此元数据元素的配置源对象（可能为空）
	 */
	Object getSource();
	
}

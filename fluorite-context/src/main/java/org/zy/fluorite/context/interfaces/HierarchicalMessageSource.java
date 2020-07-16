package org.zy.fluorite.context.interfaces;

/**
 * @DateTime 2020年6月17日 下午1:25:36;
 * @author zy(azurite-Y);
 * @Description 消息源上下级接口
 */
public interface HierarchicalMessageSource {
	/**
	 * 设置上级消息源
	 * @param parent
	 */
	void setParentMessageSource(MessageSource parent);
	
	/**
	 * 获得上级消息源
	 * @return
	 */
	MessageSource getParentMessageSource();
}

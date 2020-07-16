package org.zy.fluorite.core.interfaces;

/**
 * @DateTime 2020年6月18日 下午2:34:20;
 * @author zy(azurite-Y);
 * @Description
 */
public interface Ordered {
	/** 最高优先级 */
	public static final int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

	/** 最低优先级 */
	public static final int  LOWEST_PRECEDENCE = Integer.MAX_VALUE;
	
	int getOrder();
}

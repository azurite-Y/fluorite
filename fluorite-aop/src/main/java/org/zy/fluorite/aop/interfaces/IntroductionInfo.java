package org.zy.fluorite.aop.interfaces;

/**
 * @DateTime 2020年7月4日 下午3:08:08;
 * @author zy(azurite-Y);
 * @Description 提供描述介绍所需信息的接口
 */
public interface IntroductionInfo {
	/** 返回此顾问或建议引入的其他接口 */
	Class<?>[] getInterfaces();
}

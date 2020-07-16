package org.zy.fluorite.aop.interfaces.function;

import org.zy.fluorite.aop.support.TrueClassFilter;

/**
 * @DateTime 2020年7月4日 下午3:39:44;
 * @author zy(azurite-Y);
 * @Description 限制切点或引入与给定目标类集匹配的筛选器
 */
@FunctionalInterface
public interface ClassFilter {
	/**
	 * 判断切点是否应该应用给定的接口或目标类，也就是判断给定的Class对象是否是当前切面的匹配
	 * @param clazz - 需适配Bean的Class对象
	 * @return 
	 */
	boolean matches(Class<?> clazz);

	/** 匹配所有类的规范ClassFilter实例 */
	ClassFilter TRUE = TrueClassFilter.INSTANCE;
}

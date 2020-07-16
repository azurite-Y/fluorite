package org.zy.fluorite.core.interfaces;

import java.lang.annotation.Annotation;

import org.zy.fluorite.core.annotation.ComponentScan.Filter;

/**
 * @DateTime 2020年6月22日 上午8:58:49;
 * @author zy(azurite-Y);
 * @Description  包扫描结果过滤
 */
@FunctionalInterface
public interface TypeFilter {
	
	/**
	 * 非 FilterType.ASPECTJ 和 FilterType.REGEX的TypeFilter实现将调用此方法过滤包扫描结果
	 * @param clz
	 * @param filter
	 * @return true代表忽略当前Class对象
	 */
	boolean match(AnnotationMetadata metadata ,Filter filter);
	
	/**
	 * FilterType.ASPECTJ 和 FilterType.REGEX的TypeFilter实现将调用此方法过滤包扫描结果
	 * @param fileName
	 * @param filter
	 * @return true代表忽略扫描到的当前文件
	 */
	default boolean match(String fileName, Filter filter) { return false;}
	
	/**
	 * 为TypeFilter实现设置 ’与时俱进‘ 的相关属性
	 * @param pattern
	 * @param annos
	 * @param source
	 */
	default void invorkAware(String pattern, Class<? extends Annotation>[] annos , Class<?> source) {}
}

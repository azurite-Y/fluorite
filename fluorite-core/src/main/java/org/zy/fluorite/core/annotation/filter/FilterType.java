package org.zy.fluorite.core.annotation.filter;

/**
 * @DateTime 2020年6月22日 上午9:06:55;
 * @author zy(azurite-Y);
 * @Description 指定@Filter注解的Class行为
 */
public enum FilterType {
	/**
	 * 筛选给定注解的候选项
	 * @see org.zy.fluorite.core.annotation.filter.AnnotationTypeFilter
	 */
	ANNOTATION,

	/**
	 * 筛选指定类型的候选项，可以是给定类型的子类或其本身
	 * @see org.zy.fluorite.core.annotation.filter.AssignableTypeFilter
	 */
	ASSIGNABLE_TYPE,

	/**
	 * 筛选与给定AspectJ类型模式表达式匹配的候选项。
	 * @see org.springframework.core.type.filter.AspectJTypeFilter
	 */
	ASPECTJ,

	/**
	 * 筛选与给定正则表达式模式匹配的候选项
	 * @see org.zy.fluorite.core.annotation.filter.RegexPatternTypeFilter
	 */
	REGEX,

	/** 
	 * 使用给定的自定义筛选器过滤候选
	 * 实现{@link org.zy.fluorite.core.interfaces.TypeFilter}
	 */
	CUSTOM
}

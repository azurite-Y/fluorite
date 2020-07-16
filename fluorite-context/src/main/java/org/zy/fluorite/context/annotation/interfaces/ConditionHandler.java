package org.zy.fluorite.context.annotation.interfaces;

import org.zy.fluorite.core.subject.AnnotationAttributes;

/**
 * @DateTime 2020年6月30日 下午1:10:50;
 * @author zy(azurite-Y);
 * @Description 定义条件注解处理类的适用范围和处理方法
 */
public interface ConditionHandler<T> {
	T support(AnnotationAttributes attributes);

	boolean matches(ConditionContext context, T conditional);
}

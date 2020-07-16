package org.zy.fluorite.aop.aspectj.interfaces;

import java.lang.reflect.Method;

import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @DateTime 2020年7月6日 下午4:02:47;
 * @author zy(azurite-Y);
 * @Description
 */
public interface AspectJPrecedenceInformation  extends Ordered {
	/** 返回声明此advice的切面的beanName */
	String getAspectName();

	/** 返回方面中advice的声明顺序，也就是当前Advice在Advice列表中的索引下标 */
	int getDeclarationOrder();

	/** 是否是前置通知 */
	boolean isBeforeAdvice();

	/** 是否是后置通知 */
	boolean isAfterAdvice();

	Class<?> getDeclaringClass();

	String getMethodName();

	Method getAspectJAdviceMethod();

	Class<?>[] getParameterTypes();

}

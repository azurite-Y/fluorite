package org.zy.fluorite.aop.aspectj.advice;

import org.zy.fluorite.aop.aspectj.support.ClassFilters;
import org.zy.fluorite.aop.aspectj.support.TypePatternClassFilter;
import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.IntroductionAdvisor;
import org.zy.fluorite.aop.interfaces.IntroductionInterceptor;
import org.zy.fluorite.aop.interfaces.function.ClassFilter;

/**
 * @DateTime 2020年7月6日 下午6:06:35;
 * @author zy(azurite-Y);
 * @Description
 */
public class DeclareParentsAdvisor implements IntroductionAdvisor {

	private final Advice advice;

	private final Class<?> introducedInterface;

	private final ClassFilter typePatternClassFilter;


	/**
	 * 为此DeclareParents字段创建Advisor
	 * @param type -@DeclareParents注解标注的属性类型，引入属性的类型
	 * @param value -@DeclareParents 注解的value属性，目标类型表达式
	 * @param defaultImpl -@DeclareParents注解的defaultImpl属性，默认实现类
	 */
	public DeclareParentsAdvisor(Class<?> interfaceType, String typePattern, Class<?> defaultImpl) {
		this(interfaceType, typePattern, new DelegatePerTargetObjectIntroductionInterceptor(defaultImpl, interfaceType));
	}

	/**
	 * @param interfaceType
	 * @param typePattern
	 * @param interceptor
	 */
	private DeclareParentsAdvisor(Class<?> interfaceType, String typePattern, IntroductionInterceptor interceptor) {
		this.advice = interceptor;
		this.introducedInterface = interfaceType;

		ClassFilter typePatternFilter = new TypePatternClassFilter(typePattern);
		// 排除引入接口的实现类
		ClassFilter exclusion = (clazz -> !this.introducedInterface.isAssignableFrom(clazz));
		this.typePatternClassFilter = ClassFilters.intersection(typePatternFilter, exclusion);
	}
	
	@Override
	public ClassFilter getClassFilter() {
		return this.typePatternClassFilter;
	}

	@Override
	public void validateInterfaces() throws IllegalArgumentException {
//		if (!this.introducedInterface.isAssignableFrom(this.defaultImpl)) {
//			throw new IllegalArgumentException();
//		}
	}

	@Override
	public Advice getAdvice() {
		return this.advice;
	}

	@Override
	public Class<?>[] getInterfaces() {
		return new Class<?>[] {this.introducedInterface};
	}
}

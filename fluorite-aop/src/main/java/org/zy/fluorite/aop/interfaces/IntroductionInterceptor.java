package org.zy.fluorite.aop.interfaces;

import org.zy.fluorite.aop.interfaces.function.MethodInterceptor;

/**
 * @DateTime 2020年7月6日 下午6:15:26;
 * @author zy(azurite-Y);
 * @Description MethodInterceptor的子接口，允许拦截器实现附加接口，并通过该拦截器使代理可用。这是一个基本的AOP概念，称为引入
 */
public interface IntroductionInterceptor  extends MethodInterceptor, DynamicIntroductionAdvice {

}

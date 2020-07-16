package org.zy.fluorite.aop.interfaces;

import org.zy.fluorite.aop.aspectj.interfaces.AfterAdvice;

/**
 * @DateTime 2020年7月6日 下午11:51:51;
 * @author zy(azurite-Y);
 * @Description 用于抛出建议的标记接口
 * <ul> 有效的异常通知方法示例：（必须拥有的参数为异常参数，其他为可选参数。）
 * <li>public void afterThrowing(Exception ex)</li>
 * <li>public void afterThrowing(RemoteException)</li>
 * <li>public void afterThrowing(Method method, Object[] args, Object target, Exception ex)</li>
 * <li>public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)</li>
 * </ul>
 */
public interface ThrowsAdvice extends AfterAdvice {}

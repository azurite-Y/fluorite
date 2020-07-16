package org.zy.fluorite.aop.aspectj.interfaces;

/**
 * @DateTime 2020年7月11日 下午11:21:08;
 * @author zy(azurite-Y);
 * @Description 如果是环绕增强时，使用 {@linkplain  ProceedingJoinPoint}表示连接点对象。
 * 任何一个增强方法都可以通过将第一个入参声明为JoinPoint访问到连接点上下文的信息
 */
public interface ProceedingJoinPoint extends JoinPoint{
	 /**
     * 继续下一个通知或目标方法调用
     * @return 调用结果
     * @throws Throwable - 如果调用的进程抛出异常
     */
    public Object proceed() throws Throwable;

    /**
     * 继续下一个通知或目标方法调用
     * @param args 调用所需的参数
     * @return 调用结果
     * @throws Throwable - 如果调用的进程抛出异常
     */
    public Object proceed(Object[] args) throws Throwable;
}

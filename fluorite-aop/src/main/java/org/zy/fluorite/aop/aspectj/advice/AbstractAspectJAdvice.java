package org.zy.fluorite.aop.aspectj.advice;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.zy.fluorite.aop.aspectj.interfaces.AspectInstanceFactory;
import org.zy.fluorite.aop.aspectj.interfaces.AspectJPrecedenceInformation;
import org.zy.fluorite.aop.aspectj.interfaces.JoinPoint;
import org.zy.fluorite.aop.aspectj.interfaces.JoinPointMatch;
import org.zy.fluorite.aop.aspectj.interfaces.ProceedingJoinPoint;
import org.zy.fluorite.aop.aspectj.support.AspectJPluralisticPointcut;
import org.zy.fluorite.aop.aspectj.support.DefaultJoinPointMatch;
import org.zy.fluorite.aop.aspectj.support.MethodInvocationProceedingJoinPoint;
import org.zy.fluorite.aop.exception.AopInvocationException;
import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.Joinpoint;
import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.Pointcut;
import org.zy.fluorite.aop.interfaces.ProxyMethodInvocation;
import org.zy.fluorite.aop.support.ExposeInvocationInterceptor;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年7月6日 下午4:07:49;
 * @author zy(azurite-Y);
 * @Description
 */
@SuppressWarnings("serial")
public abstract class AbstractAspectJAdvice  implements Advice, AspectJPrecedenceInformation, Serializable  {
	protected static final String JOIN_POINT_KEY = JoinPoint.class.getName();
	protected static final String JOIN_POINT_MATCH = JoinPointMatch.class.getName();

	
	private final Class<?> declaringClass;

	private final String methodName;

	private final Class<?>[] parameterTypes;

	protected transient Method aspectJAdviceMethod;

	private final AspectJPluralisticPointcut pointcut;

	private final AspectInstanceFactory aspectInstanceFactory;
	
	/** 切面的beanName */
	private String aspectName = "";

	/** Advice的声明顺序 */
	private int declarationOrder;

	/** 存储异常通知方法的异常对象参数类型 */
	private Class<?> throwingType = Object.class;
	
	/** 目标方法参数类型 */
	private Class<?>[] pointcutParameterTypes = new Class<?>[0];
	
	/** 是否已进行参数检查和绑定的标识 */
	private boolean argumentsIntrospected = false;
	
	/** 此JoinPoint参数的索引 */
	private int joinPointArgumentIndex = -1;
	
//	private int joinPointStaticPartArgumentIndex = -1;
	
	public AbstractAspectJAdvice(Method aspectJAdviceMethod, AspectJPluralisticPointcut pointcut, AspectInstanceFactory aspectInstanceFactory) {
		Assert.notNull(aspectJAdviceMethod, "Advice不能为null");
		this.declaringClass = aspectJAdviceMethod.getDeclaringClass();
		this.methodName = aspectJAdviceMethod.getName();
		this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
		this.aspectJAdviceMethod = aspectJAdviceMethod;
		this.pointcut = pointcut;
		this.aspectInstanceFactory = aspectInstanceFactory;
	}

	public final Method getAspectJAdviceMethod() {
		return this.aspectJAdviceMethod;
	}

	public final AspectInstanceFactory getAspectInstanceFactory() {
		return this.aspectInstanceFactory;
	}
	
	public final ClassLoader getAspectClassLoader() {
		return this.aspectInstanceFactory.getAspectClassLoader();
	}
	
	public final AspectJPluralisticPointcut getPointcut() {
		calculateArgumentBindings();
		return this.pointcut;
	}
	
	/** 作为设置的一部分，尽可能多地做一些工作，以便在后续的通知调用上尽可能快地绑定参数 */
	public final synchronized void calculateArgumentBindings() {
		if (this.argumentsIntrospected || this.parameterTypes.length == 0) {
			return;
		}

		Class<?>[] parameterTypes = this.aspectJAdviceMethod.getParameterTypes();
		if (maybeBindJoinPoint(parameterTypes[0]) || maybeBindProceedingJoinPoint(parameterTypes[0]) 
		/* || maybeBindJoinPointStaticPart(parameterTypes[0]) */) {
			this.argumentsIntrospected = true;
		}
	}
	
//	private boolean maybeBindJoinPointStaticPart(Class<?> candidateParameterType) {
//		if (JoinPoint.StaticPart.class == candidateParameterType) {
//			this.joinPointStaticPartArgumentIndex = 0;
//			return true;
//		} else {
//			return false;
//		}
//	}

	/**
	 * 确定环绕方法的第一个单数是否是 {@linkplain ProceedingJoinPoint }类型
	 * @param candidateParameterType - 通知方法的第一个参数
	 * @return
	 */
	private boolean maybeBindProceedingJoinPoint(Class<?> candidateParameterType) {
		if (ProceedingJoinPoint.class == candidateParameterType) {
			if (!supportsProceedingJoinPoint()) {
				throw new IllegalArgumentException("ProceedingJoinPoint类型的参数仅支持环绕通知方法");
			}
			this.joinPointArgumentIndex = 0;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 确定环绕方法的第一个单数是否是 {@linkplain Joinpoint }类型
	 * @param candidateParameterType - 通知方法的第一个参数
	 * @return
	 */
	private boolean maybeBindJoinPoint(Class<?> candidateParameterType) {
		if (JoinPoint.class == candidateParameterType) {
			this.joinPointArgumentIndex = 0;
			return true;
		} else {
			return false;
		}
	}

	protected Object invokeAdviceMethod(JoinPoint pjp, JoinPointMatch jpm, Object returnValue, Throwable ex) throws Throwable {
		return invokeAdviceMethodWithGivenArgs(argBinding(pjp, jpm, returnValue, ex));
	}
	
	/**
	 * 调用advice方法。
	 * @param jpMatch - 与此执行连接点匹配的JoinPointMatch
	 * @param returnValue - 方法执行的返回值（可以为null）
	 * @param ex - 方法执行引发的异常（可能为null）
	 * @return 调用结果
	 * @throws Throwable - 调用失败则抛出异常
	 */
	protected Object invokeAdviceMethod(	JoinPointMatch jpMatch, Object returnValue, Throwable ex)	throws Throwable {
		return invokeAdviceMethodWithGivenArgs(argBinding(getJoinPoint(), jpMatch, returnValue, ex));
	}

	private Object invokeAdviceMethodWithGivenArgs(Object[] argBinding)  throws Throwable {
		Object[] actualArgs = argBinding;
		if (this.aspectJAdviceMethod.getParameterCount() == 0) {
			actualArgs = null;
		}
		try {
			ReflectionUtils.makeAccessible(this.aspectJAdviceMethod);
			// 调用织入的Advice方法
			return this.aspectJAdviceMethod.invoke(this.aspectInstanceFactory.getAspectInstance(), actualArgs);
		} catch (IllegalArgumentException ex) {
			throw new AopInvocationException("advice方法的参数不匹配 [" + this.aspectJAdviceMethod + "]，by aspectClass："+this.aspectName , ex);
		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

	private Object[] argBinding(JoinPoint joinPoint, JoinPointMatch jpMatch, Object returnValue, Throwable ex) {
		calculateArgumentBindings();

		Object[] adviceInvocationArgs = new Object[this.parameterTypes.length];
		int numBound = 0;

		if (this.joinPointArgumentIndex != -1) {
			adviceInvocationArgs[this.joinPointArgumentIndex] = joinPoint;
			numBound++;
		}
//		else if (this.joinPointStaticPartArgumentIndex != -1) {
//			adviceInvocationArgs[this.joinPointStaticPartArgumentIndex] = joinPoint.getStaticPart();
//			numBound++;
//		}
		 
		if (numBound != adviceInvocationArgs.length) {  // 条件成立则代表此Advice方法不只有一个参数
			Class<?> clz = null;
			Parameter[] parameters = aspectJAdviceMethod.getParameters();
			// 根据通知方法的参数类型绑定参数
			for (int i = numBound; i < this.parameterTypes.length ; i++) {
				clz = this.parameterTypes[i];
				
				if (returnValue != null &&clz.isInstance(returnValue) ) { // 返回值只有在目标方法调用之后才可获取，否则为null
					adviceInvocationArgs[i] = returnValue;
					continue;
				} else if (ex != null && clz.isInstance(ex) ) { // 异常参数只有在目标方法抛出异常之后才可获取，否则为null
					adviceInvocationArgs[i] = ex;
					continue;
				}
				
				if (jpMatch != null) { // 匹配目标方法的参数，如果有的话
					Object parameterBinding = jpMatch.parameterBinding(clz, parameters[i].getName());
					if (parameterBinding != null) {
						adviceInvocationArgs[i] = parameterBinding;
					}
				}
				Assert.notNull(adviceInvocationArgs[i],"不合规的参数类型 ["+clz.getName()+"]，无法找到对应的参数。on："+aspectJAdviceMethod.toString());
			}
		}
		return adviceInvocationArgs;
	}

	private JoinPoint getJoinPoint() {
		return currentJoinPoint();
	}

	/** 延迟实例化当前的joinpoint调用。需要要与ExposeInvocationInterceptor绑定的MethodInvocation。 */
	public static JoinPoint currentJoinPoint() {
		MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
		if (!(mi instanceof ProxyMethodInvocation)) {
			throw new IllegalStateException("提供的MethodInvocation对象未实现ProxyMethodInvocation接口，by："+mi);
		}
		ProxyMethodInvocation pmi = (ProxyMethodInvocation) mi;
		JoinPoint jp = (JoinPoint) pmi.getUserAttribute(JOIN_POINT_KEY);
		if (jp == null) {
			jp = new MethodInvocationProceedingJoinPoint(pmi);
			pmi.setUserAttribute(JOIN_POINT_KEY, jp);
		}
		return jp;
	}
	
	@Override
	public int getOrder() {
		return this.aspectInstanceFactory.getOrder();
	}

	@Override
	public String getAspectName() {
		return this.aspectInstanceFactory.getName();
	}

	public void setAspectName(String aspectName) {
		this.aspectName = aspectName;
	}

	@Override
	public int getDeclarationOrder() {
		return this.declarationOrder;
	}

	public void setDeclarationOrder(int order) {
		this.declarationOrder = order;
	}

	/**
	 * 根据此方法的返回值确定是否还继续调用切点方法
	 * @return
	 */
	protected boolean supportsProceedingJoinPoint() {
		return false;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return this.declaringClass;
	}

	@Override
	public String getMethodName() {
		return this.methodName;
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return this.parameterTypes;
	}

	public Class<?>[] getPointcutParameterTypes() {
		return pointcutParameterTypes;
	}

	public Class<?> getThrowingType() {
		return throwingType;
	}

	public void setThrowingType(Class<?> throwingType) {
		this.throwingType = throwingType;
	}

	public void setPointcutParameterTypes(Class<?>[] pointcutParameterTypes) {
		this.pointcutParameterTypes = pointcutParameterTypes;
	}

	public Pointcut buildSafePointcut() {
		throw new IllegalArgumentException("此方法暂未实现...");
	}
	
	protected JoinPointMatch getJoinPointMatch() {
		MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
		if (!(mi instanceof ProxyMethodInvocation)) {
			throw new IllegalStateException("提供的MethodInvocation实例必须实现ProxyMethodInvocation接口: " + mi);
		}
		return getJoinPointMatch((ProxyMethodInvocation) mi);
	}

	/**
	 * 从MethodInvocation中获得JoinPointMatch对象，若为null则创建一个JoinPointMatch并保存到MethodInvocation中
	 * @param pmi - 当前线程所调用的MethodInvocation实例
	 * @return 若调用方法为无参方法则返回null，反之则返回一个JoinPointMatch实例
	 */
	protected JoinPointMatch getJoinPointMatch(ProxyMethodInvocation pmi) {
		JoinPointMatch jpMatch = (JoinPointMatch) pmi.getUserAttribute(JOIN_POINT_MATCH);
		if (jpMatch == null && pmi.getArguments().length > 0) {
			jpMatch = new DefaultJoinPointMatch(pmi.getArguments());
			pmi.setUserAttribute(JOIN_POINT_MATCH, jpMatch);
		}
		return jpMatch;
	}
}

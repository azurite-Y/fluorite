package org.zy.fluorite.aop.aspectj.advice;

import java.util.Map;
import java.util.WeakHashMap;

import org.zy.fluorite.aop.interfaces.DynamicIntroductionAdvice;
import org.zy.fluorite.aop.interfaces.IntroductionInterceptor;
import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.ProxyMethodInvocation;
import org.zy.fluorite.aop.support.IntroductionInfoSupport;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年7月7日 下午4:11:44;
 * @author zy(azurite-Y);
 * @Description 这与DelegatingIntroductionInterceptor的不同之处在于，该类的一个实例可用于通知多个目标对象，
 * 并且每个targetobject都将有自己的委托（而DelegatingIntroductionInterceptor共享同一个委托，因此所有目标的状态相同）。
 * <p>suppressInterface方法可用于禁止由委托类实现的interfacesimplemented，但不应将其引入拥有的AOP代理。</p>
 */
@SuppressWarnings("serial")
public class DelegatePerTargetObjectIntroductionInterceptor extends IntroductionInfoSupport implements IntroductionInterceptor {
	private final Map<Object, Object> delegateMap = new WeakHashMap<>();
	
	/** {@code @DeclareParents }注解的defaultImpl()属性  */
	private Class<?> defaultImplType;

	/** {@code @DeclareParents }注解标注的属性类型 */
	private Class<?> interfaceType;

	public DelegatePerTargetObjectIntroductionInterceptor(Class<?> defaultImplType, Class<?> interfaceType) {
		this.defaultImplType = defaultImplType;
		this.interfaceType = interfaceType;
		
		// 创建@DeclareParents注解指定的默认实现的实例
//		Object delegate = createNewDelegate();
		
		// 提取此实例所实现的所有接口
		implementInterfacesOnObject(defaultImplType);
		// 设置限制公开的接口
		suppressInterface(IntroductionInterceptor.class);
		suppressInterface(DynamicIntroductionAdvice.class);
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if (isMethodOnIntroducedInterface(invocation)) {
			// 获得@DeclareParents注解指定的默认实现的实例
			Object delegate = getIntroductionDelegateFor(invocation.getThis());
			// 反射调用
			Object result = ReflectionUtils.invokeMethod(delegate, invocation.getMethod(), invocation.getArguments());

			// 若引入的接口方法实现 "return this;"则再次替换为返回代理对象
			if (result == delegate && invocation instanceof ProxyMethodInvocation) {
				result = ((ProxyMethodInvocation) invocation).getProxy();
			}
			return result;
		}
		return doProceed(invocation);
	}

	protected Object doProceed(MethodInvocation invocation) throws Throwable {
		return invocation.proceed();
	}

	/**
	 * 获得引入接口的实现类实例
	 * @param targetObject - 被增强的Bean实例
	 * @return
	 */
	private Object getIntroductionDelegateFor(Object targetObject) {
		synchronized (this.delegateMap) {
			if (this.delegateMap.containsKey(targetObject)) {
				return this.delegateMap.get(targetObject);
			} else {
				Object delegate = createNewDelegate();
				this.delegateMap.put(targetObject, delegate);
				return delegate;
			}
		}
	}

	private Object createNewDelegate() {
		try {
			return ReflectionUtils.accessibleConstructor(this.defaultImplType).newInstance();
		} catch (Throwable ex) {
			throw new IllegalArgumentException("创建默认实现失败，by type："+this.defaultImplType.getName()+"，interface："
					+ this.interfaceType.getName() , ex);
		}
	}
}

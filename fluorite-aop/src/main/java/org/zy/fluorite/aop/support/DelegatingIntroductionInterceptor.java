package org.zy.fluorite.aop.support;

import org.zy.fluorite.aop.interfaces.DynamicIntroductionAdvice;
import org.zy.fluorite.aop.interfaces.IntroductionInterceptor;
import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.aop.interfaces.ProxyMethodInvocation;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年7月7日 下午3:53:36;
 * @author zy(azurite-Y);
 * @Description 子类只需要扩展这个类并实现自己要引入的接口。在这种情况下，委托就是子类实例本身。或者，一个单独的委托可以实现该接口，并通过委托bean属性进行设置。
 * <p>委托或子类可以实现任意数量的接口。默认情况下，除导入拦截器外的所有接口都从子类或委托中获得。</p>
 * <p>suppressInterface方法可以用来抑制由委托实现但不应该引入到owningAOP代理的接口。</p>
 * <p>如果委托是可序列化的，则该类的实例是可序列化的。</p>
 */
@SuppressWarnings("serial")
public class DelegatingIntroductionInterceptor extends IntroductionInfoSupport implements IntroductionInterceptor {
	private Object delegate;

	/**
	 * 构造一个新的DelegatingIntroductionInterceptor，提供一个实现要引入的接口的对象
	 * @param delegate the delegate that implements the introduced interfaces
	 */
	public DelegatingIntroductionInterceptor(Object delegate) {
		init(delegate);
	}

	/**
	 * 构造一个新的DelegatingIntroductionInterceptor，
	 * 委托将是子类，它必须实现其他接口
	 */
	protected DelegatingIntroductionInterceptor() {
		init(this);
	}

	private void init(Object delegate) {
		Assert.notNull(delegate, "Delegate不能为nullnull");
		this.delegate = delegate;
		implementInterfacesOnObject(delegate.getClass());

		// 设置不想公开的接口
		suppressInterface(IntroductionInterceptor.class);
		suppressInterface(DynamicIntroductionAdvice.class);
	}

	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		if (isMethodOnIntroducedInterface(mi)) {
			// 如果引入的方法抛出异常，使用下面的方法而不是直接反射
			Object result = ReflectionUtils.invokeMethod(this.delegate, mi.getMethod(), mi.getArguments());

			if (result == this.delegate && mi instanceof ProxyMethodInvocation) {
				Object proxy = ((ProxyMethodInvocation) mi).getProxy();
				if (mi.getMethod().getReturnType().isInstance(proxy)) {
					result = proxy;
				}
			}
			return result;
		}

		return doProceed(mi);
	}

	/**
	 * 子类可以重写这个方法来拦截对目标对象的方法调用，这在引入需要监视被引入的对象时很有用。
	 * 对于引入的接口的方法调用，永远不会调用此方法
	 */
	protected Object doProceed(MethodInvocation mi) throws Throwable {
		return mi.proceed();
	}

}

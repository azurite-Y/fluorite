package org.zy.fluorite.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.zy.fluorite.aop.interfaces.IntroductionInfo;
import org.zy.fluorite.aop.interfaces.MethodInvocation;
import org.zy.fluorite.core.utils.ClassUtils;

/**
 * @DateTime 2020年7月7日 下午3:55:25;
 * @author zy(azurite-Y);
 * @Description 允许子类方便地从给定对象添加所有接口，并禁止不应添加的接口。还允许查询所有引入的接口
 */
@SuppressWarnings("serial")
public class IntroductionInfoSupport implements IntroductionInfo, Serializable {
	protected final Set<Class<?>> publishedInterfaces = new LinkedHashSet<>();

	private transient Map<Method, Boolean> rememberedMethods = new ConcurrentHashMap<>(32);

	/**
	 * 抑制指定的接口，该接口可能已由实现它的委托自动检测到。
	 * 调用此方法以排除内部接口在代理级别可见。 如果接口不是由委托实现的，则不执行任何操作
	 */
	public void suppressInterface(Class<?> ifc) {
		this.publishedInterfaces.remove(ifc);
	}

	@Override
	public Class<?>[] getInterfaces() {
		return ClassUtils.toClassArray(this.publishedInterfaces);
	}

	/** 检查指定接口是否为已发布的引入接口 */
	public boolean implementsInterface(Class<?> ifc) {
		for (Class<?> pubIfc : this.publishedInterfaces) {
			if (ifc.isInterface() && ifc.isAssignableFrom(pubIfc)) {
				return true;
			}
		}
		return false;
	}

	/** 发布给定对象在代理级别实现的所有接口 */
	protected void implementInterfacesOnObject(Class<?> delegate) {
		this.publishedInterfaces.addAll(ClassUtils.getAllInterfacesToSet(delegate));
	}

	/**
	 * 判断此方法是否在引入的接口中定义
	 * 
	 * @param invocation - 方法调用
	 * @return 调用的方法是否在引入的接口上
	 */
	protected final boolean isMethodOnIntroducedInterface(MethodInvocation invocation) {
		Boolean rememberedResult = this.rememberedMethods.get(invocation.getMethod());
		if (rememberedResult != null) {
			return rememberedResult;
		} else {
			boolean result = implementsInterface(invocation.getMethod().getDeclaringClass());
			this.rememberedMethods.put(invocation.getMethod(), result);
			return result;
		}
	}

}

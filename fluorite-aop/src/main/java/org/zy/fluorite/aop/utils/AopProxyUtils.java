package org.zy.fluorite.aop.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.zy.fluorite.aop.interfaces.Advised;
import org.zy.fluorite.aop.interfaces.DecoratingProxy;
import org.zy.fluorite.aop.interfaces.FluoriteProxy;
import org.zy.fluorite.aop.interfaces.TargetClassAware;
import org.zy.fluorite.aop.interfaces.TargetSource;
import org.zy.fluorite.aop.support.AdvisedSupport;
import org.zy.fluorite.aop.target.SingletonTargetSource;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年6月18日 下午1:06:20;
 * @author zy(azurite-Y);
 * @Description
 */
public class AopProxyUtils {
	public static final String ORIGINAL_TARGET_CLASS_ATTRIBUTE = StringUtils.append(".", AopProxyUtils.class.getName(),"originalTargetClass");
	
	/** 
	 * Bean definition属性，它可以指示给定的Bean是否应该用它的目标类进行代理（如果它首先被代理）。值是true或者false
	 * 如果代理工厂为某个特定的bean构建了一个目标类代理，并且希望强制该bean始终可以被强制转换到其目标类（即使AOP建议是通过自动代理应用的），则可以设置此属性。
	 */
	private static final String PRESERVE_TARGET_CLASS_ATTRIBUTE = StringUtils.append(".", AopProxyUtils.class.getName(),"preserveTargetClass");

	/**
	 * 判断指定bean是否为AOP相关接口，是则调用相关方法返回其顶级目标类
	 * @return
	 */
	public static Object getSingletonTarget(Object bean) {
		if (bean instanceof Advised) {
			TargetSource targetSource = ((Advised) bean).getTargetSource();
			if (targetSource instanceof SingletonTargetSource) {
				return ((SingletonTargetSource) targetSource).getTarget();
			}
		}
		return null;
	}

	/**
	 * 将原始的切点类保存到BeanDefinition的 属性中
	 * @param beanFactory
	 * @param beanName
	 * @param beanClass
	 */
	public static void exposeTargetClass(ConfigurableListableBeanFactory beanFactory, String beanName,Class<? extends Object> beanClass) {
		if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
			beanFactory.getBeanDefinition(beanName).setAttribute(ORIGINAL_TARGET_CLASS_ATTRIBUTE, beanClass);
		}
	}

	/**
	 * 为给定的AOP配置确定要代理的完整接口集
	 * @param advised - 代理配置类
	 * @return 有个为此代理设置的接口集
	 */
	public static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised) {
		return completeProxiedInterfaces(advised, false);
	}

	/**
	 * 为给定的AOP配置确定要代理的完整接口集
	 * @param advised - 代理配置类
	 * @param decoratingProxy - 是否公开DecoratingProxy接口
	 * @return 代理的完整接口集
	 */
	public static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised, boolean decoratingProxy) {
		Class<?>[] specifiedInterfaces = advised.getProxiedInterfaces();
		if (specifiedInterfaces.length == 0) {
			Class<?> targetClass = advised.getTargetClass();
			if (targetClass != null) {
				if (targetClass.isInterface()) { // 代理类是接口则添加到接口集中
					advised.setInterfaces(targetClass);
				} else if (Proxy.isProxyClass(targetClass)) { // 代理类通过jdk动态代理生成则获取其实现接口并添加到接口集中
					advised.setInterfaces(targetClass.getInterfaces());
				}
				// 设置找到的接口集
				specifiedInterfaces = advised.getProxiedInterfaces();
			}
		}
		// 若代理类接口集中没有对应的接口则返回true
		boolean addSpringProxy = !advised.isInterfaceProxied(FluoriteProxy.class);
		boolean addAdvised = !advised.isOpaque() && !advised.isInterfaceProxied(Advised.class);
		boolean addDecoratingProxy = (decoratingProxy && !advised.isInterfaceProxied(DecoratingProxy.class));
		int nonUserIfcCount = 0;
		if (addSpringProxy) {
			nonUserIfcCount++;
		}
		if (addAdvised) {
			nonUserIfcCount++;
		}
		if (addDecoratingProxy) {
			nonUserIfcCount++;
		}
		Class<?>[] proxiedInterfaces = new Class<?>[specifiedInterfaces.length + nonUserIfcCount];
		// 复制之前找到的接口集
		System.arraycopy(specifiedInterfaces, 0, proxiedInterfaces, 0, specifiedInterfaces.length);
		int index = specifiedInterfaces.length;
		// 根据之前的判断添加接口
		if (addSpringProxy) {
			proxiedInterfaces[index] = FluoriteProxy.class;
			index++;
		}
		if (addAdvised) {
			proxiedInterfaces[index] = Advised.class;
			index++;
		}
		if (addDecoratingProxy) {
			proxiedInterfaces[index] = DecoratingProxy.class;
		}
		return proxiedInterfaces;
	}

	/**
	 * 检查给定AdvisedSupport对象背后的代理是否相等
	 * @param advised
	 * @param otherAdvised
	 * @return
	 */
	public static boolean equalsInProxy(AdvisedSupport a, AdvisedSupport b) {
		return (a == b ||
				(equalsProxiedInterfaces(a, b) && equalsAdvisors(a, b) && a.getTargetSource().equals(b.getTargetSource())));
	}
	
	/** 检查给定AdvisedSupport对象背后的代理接口是否相等 */
	public static boolean equalsProxiedInterfaces(AdvisedSupport a, AdvisedSupport b) {
		return Arrays.equals(a.getProxiedInterfaces(), b.getProxiedInterfaces());
	}

	/** 检查给定AdvisedSupport对象背后的顾问是否相等 */
	public static boolean equalsAdvisors(AdvisedSupport a, AdvisedSupport b) {
		return Arrays.equals(a.getAdvisors(), b.getAdvisors());
	}

	/**
	 * 确定是否应该用它的targetclass而不是它的接口来代理给定的bean。检查相应bean定义的“preserveTargetClass”属性。
	 * @param beanFactory
	 * @param beanName
	 * @return
	 */
	public static boolean shouldProxyTargetClass(ConfigurableListableBeanFactory beanFactory, String beanName) {
		if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
			BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
			return Boolean.TRUE.equals(bd.getAttribute(PRESERVE_TARGET_CLASS_ATTRIBUTE));
		}
		return false;
	}

	/**
	 * 如果需要，请将给定参数调整为给定方法中的目标签名：
	 * 特别是，如果给定的vararg参数数组与方法中声明的vararg参数的数组类型不匹配
	 * @param method
	 * @param args
	 * @return
	 */
	public static Object[] adaptArgumentsIfNecessary(Method method, Object[] args) {
		return args;
	}

	/**
	 * 确定给定实例的顶级目标类
	 * @param candidate
	 * @return
	 */
	public static Class<?> ultimateTargetClass(Object candidate) {
		Assert.notNull(candidate, "'candidate'对象不能为null");
		Object current = candidate;
		Class<?> result = null;
		while (current instanceof TargetClassAware) {
			result = ((TargetClassAware) current).getTargetClass();
			current = getSingletonTarget(current);
		}
		if (result == null) {
			result = (AopUtils.isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
		}
		return result;
	}
	
}

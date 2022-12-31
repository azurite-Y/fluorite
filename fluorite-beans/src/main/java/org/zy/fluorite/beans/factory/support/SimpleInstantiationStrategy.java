package org.zy.fluorite.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.exception.BeanInstantiationException;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.InstantiationStrategy;
import org.zy.fluorite.beans.factory.utils.BeanUtils;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月12日 下午4:39:11;
 * @Description 实例化策略
 */
public class SimpleInstantiationStrategy implements InstantiationStrategy {
	private static final ThreadLocal<Method> currentlyInvokedFactoryMethod = new ThreadLocal<>();
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static Method getCurrentlyinvokedfactorymethod() {
		return currentlyInvokedFactoryMethod.get();
	}

	@Override
	public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner) throws BeansException {
		if (!bd.hasMethodOverrides()) {
			Constructor<?> constructorToUse;
			synchronized (bd.getConstructorArgumentLock()) {
				constructorToUse = (Constructor<?>) bd.getResolvedConstructorOrFactoryMethod();
				if (constructorToUse == null) {
					final Class<?> clazz = bd.getBeanClass();
					if (clazz.isInterface()) {
						throw new BeanInstantiationException("指定的Bean为接口，无法创建对象。by："+clazz);
					}
					try {
						// 获得无参构造器
						constructorToUse = clazz.getDeclaredConstructor();
						bd.setResolvedConstructorOrFactoryMethod(constructorToUse);
					} catch (Throwable ex) {
						throw new BeanInstantiationException("未找到默认的构造器，by："+clazz);
					}
				}
			}
			Object instantiateClass = BeanUtils.instantiateClass(constructorToUse);
			DebugUtils.log(logger, "通过无参构造器创建实例成功，by name："+beanName);
			return instantiateClass;
		}
		else {
			// 必须生成CGLIB子类
			return instantiateWithMethodInjection(bd, beanName, owner, null, null);
		}

	}

	@Override
	public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner, Constructor<?> ctor,Object... args) throws BeansException {
		if (!bd.hasMethodOverrides()) {
			Object instantiateClass = BeanUtils.instantiateClass(ctor, args);
			DebugUtils.log(logger, "通过给定的构造器创建实例成功，by name："+beanName +"，Constructor："+ctor.getName() +"，args："+(args != null ? Arrays.asList(args) : args));
			return instantiateClass;
		} else {
			return instantiateWithMethodInjection(bd, beanName, owner, ctor, args);
		}
	}

	@Override
	public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner
			, Object factoryBean,Method factoryMethod, Object... args) throws BeansException {
		try {
			ReflectionUtils.makeAccessible(factoryMethod);
			// 预先保存当前正在调用的工厂方法
			Method priorInvokedFactoryMethod = currentlyInvokedFactoryMethod.get();
			try {
				currentlyInvokedFactoryMethod.set(factoryMethod);
				/**
				 * 反射调用Method对象指代的方法
				 * 【lite-实例化对象】@Component类中的@Bean方法：简单的@Bean方法的反射调用，factoryBean为@Component类
				 * 【full-实例化对象】@Configuration类中的@Bean方法： 
				 * 虽说也是@Bean方法的反射调用，但最终会调用ConfigurationClassEnhancer$BeanMethodInterceptor.intercept()方法
				 */
				Object result = factoryMethod.invoke(factoryBean, args);
				DebugUtils.log(logger, "通过工厂方法创建实例成功，by name："+beanName+"，factoryMethod："
						+factoryMethod.getName()+"，factoryBean："+factoryBean.getClass().getSimpleName()+ " ，args："+ (args != null ? Arrays.asList(args) : args));
				if (result == null) {
					result = new NullBean();
				}
				return result;
			} finally {
				if (priorInvokedFactoryMethod != null) {
					currentlyInvokedFactoryMethod.set(priorInvokedFactoryMethod);
				} else {
					currentlyInvokedFactoryMethod.remove();
				}
			}
		} catch (InvocationTargetException e) {
			throw new BeanInstantiationException("工厂方法调用异常，by method ：" + factoryMethod + "args: " +(args != null ? Arrays.asList(args) : args) ,e.getCause());
		} catch (Exception ex) {
			throw new BeanInstantiationException("工厂方法调用异常，by method ：" + factoryMethod + "args: " +(args != null ? Arrays.asList(args) : args) ,ex);
		}
	}

	private Object instantiateWithMethodInjection(RootBeanDefinition bd, String beanName, BeanFactory owner,
			Constructor<?> ctor, Object[] args) {
		// TODO 自动生成的方法存根
		return null;
	}
}

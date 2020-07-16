package org.zy.fluorite.beans.factory.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.factory.interfaces.processor.DestructionAwareBeanPostProcessor;
import org.zy.fluorite.core.interfaces.instantiation.DisposableBean;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月11日 下午5:35:13;
 * @Description
 */
@SuppressWarnings("serial")
public class DisposableBeanAdapter implements DisposableBean, Serializable, Runnable{

	protected static final String CLOSE_METHOD_NAME = "close";

	protected static final String SHUTDOWN_METHOD_NAME = "shutdown";

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected final Object bean;

	protected RootBeanDefinition definition;

	protected final boolean invokeDisposableBean;

	protected boolean nonPublicAccessAllowed = true;

	protected List<DestructionAwareBeanPostProcessor> beanPostProcessors = new ArrayList<>();

	public DisposableBeanAdapter(Object bean, String beanName, RootBeanDefinition mbd,List<DestructionAwareBeanPostProcessor> destructionList) {
		Assert.notNull(bean, "销毁bean对象不能为null");
		Assert.hasText(beanName, "'beanName'不可为null");
		this.bean = bean;
		// 在注册时就已经保证此值不为null
		this.definition = mbd;
		this.beanPostProcessors = destructionList;
		this.invokeDisposableBean =
				(this.bean instanceof DisposableBean);
		this.nonPublicAccessAllowed = mbd.isNonPublicAccessAllowed();
	}

	public DisposableBeanAdapter(Object bean, List<DestructionAwareBeanPostProcessor> list) {
		this.beanPostProcessors = list;
		this.bean = bean;
		this.invokeDisposableBean = this.bean instanceof DisposableBean;

	}

	@Override
	public void destroy() throws Exception {
		if (this.invokeDisposableBean) {
			if (DebugUtils.debug) {
				logger.info("调用DisposableBean实现类的destroy()方法，by bean："+this.definition.getBeanName());
			}
			try {
				((DisposableBean) this.bean).destroy();
			} catch (Exception e) {
				logger.warn("调用DisposableBean实现类的destroy()方法抛出异常，by bean："+this.definition.getBeanName());
				e.printStackTrace();
			}
		}
		
		if ( !this.beanPostProcessors.isEmpty() ) {
			for (DestructionAwareBeanPostProcessor processor : this.beanPostProcessors) {
				processor.postProcessBeforeDestruction(this.bean, this.definition);
			}
		}

		// 在此调用自定义的初始化bean方法
		Set<String> destroyMethodNames = this.definition.getDestroyMethods();
		Class<? extends Object> clz = bean.getClass();
		for (String destroyMethod : destroyMethodNames) {
			if (Assert.hasText(destroyMethod)) {
				// 若实现了InitializingBean接口则跳过
				if (this.invokeDisposableBean && destroyMethod.equals("destroy")) {
					continue;
				}
				try {
					Method method = clz.getMethod(destroyMethod);
					method.invoke(bean);
				} catch (Exception e) {
					logger.warn("调用Bean对象的初始化方法'" + destroyMethod + "()'方法抛出异常，by bean：" + this.definition.getBeanName());
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void run() {
		try {
			destroy();
		} catch (Exception e) {
			logger.warn("Bean后处理器调用销毁方法抛出异常，by bean："+this.definition.getBeanName());
			e.printStackTrace();
		}
	}

}

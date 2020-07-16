package org.zy.fluorite.aop.support;

import java.io.Closeable;
import java.util.Arrays;

import org.zy.fluorite.aop.interfaces.AopInfrastructureBean;
import org.zy.fluorite.aop.proxy.ProxyConfig;
import org.zy.fluorite.aop.proxy.ProxyFactory;
import org.zy.fluorite.core.interfaces.Aware;
import org.zy.fluorite.core.interfaces.Ordered;
import org.zy.fluorite.core.interfaces.instantiation.DisposableBean;
import org.zy.fluorite.core.interfaces.instantiation.InitializingBean;

/**
 * @DateTime 2020年7月4日 下午1:28:33;
 * @author zy(azurite-Y);
 * @Description 具有代理处理器通用功能的基类
 */
@SuppressWarnings("serial")
public class ProxyProcessorSupport extends ProxyConfig implements Ordered, AopInfrastructureBean {
	private int order = Ordered.LOWEST_PRECEDENCE;

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return order;
	}

	/**
	 * 检查给定bean类上的接口，并将它们应用于代理工厂（如果合适）。
	 * 调用isConfigurationCallbackInterface()和isInternalLanguageInterfaceto()方法以筛选合理的代理接口，
	 * 否则返回到目标类代理
	 */
	protected void evaluateProxyInterfaces(Class<?> beanClass, ProxyFactory proxyFactory) {
		Class<?>[] targetInterfaces = beanClass.getInterfaces();
		boolean hasReasonableProxyInterface = false;
		for (Class<?> ifc : targetInterfaces) {
			if (!isConfigurationCallbackInterface(ifc) && !isInternalLanguageInterface(ifc) && ifc.getMethods().length > 0) {
				hasReasonableProxyInterface = true;
				break;
			}
		}
		if (hasReasonableProxyInterface) {
			// 必须允许引入；不能只将接口设置为目标的接口
			for (Class<?> ifc : targetInterfaces) {
				proxyFactory.addInterface(ifc);
			}
		} else {
			proxyFactory.setProxyTargetClass(true);
		}
	}

	/**
	 * 确定给定接口是否只是一个容器回调，因此不应被视为合理的代理接口。
	 * 如果没有为给定bean找到合理的代理接口，那么它将使用其完整的目标类进行代理
	 */
	protected boolean isConfigurationCallbackInterface(Class<?> ifc) {
		return (InitializingBean.class == ifc || DisposableBean.class == ifc || Closeable.class == ifc
				|| AutoCloseable.class == ifc || Arrays.asList(ifc.getInterfaces()).contains(Aware.class) );
	}

	/**
	 * 确定给定接口是否是已知的内部语言接口，因此不应将其视为合理的代理接口。
	 * 如果没有为给定bean找到合理的代理接口，那么它将使用其完整的目标类进行代理，假设这是用户的意图
	 */
	protected boolean isInternalLanguageInterface(Class<?> ifc) {
		return ifc.getName().endsWith(".cglib.proxy.Factory");
	}

}

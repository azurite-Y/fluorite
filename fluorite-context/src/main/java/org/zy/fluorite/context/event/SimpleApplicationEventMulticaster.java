package org.zy.fluorite.context.event;

import java.util.concurrent.Executor;

import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.context.event.interfaces.ApplicationListener;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.interfaces.function.ErrorHandler;

/**
 * @DateTime 2020年6月18日 下午1:53:53;
 * @author zy(azurite-Y);
 * @Description ApplicationEventMulticaster接口的简单实现。
 * 将所有事件多播给所有注册的侦听器，让侦听器忽略它们不感兴趣的事件听众通常会对传入的事件对象执行相应的检查实例
 */
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {
	/** 执行器 */
	private Executor taskExecutor;
	/** 异常处理器 */
	private ErrorHandler errorHandler;

	public SimpleApplicationEventMulticaster() {}
	public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
		setBeanFactory(beanFactory);
	}


	/**
	 * 设置自定义执行器
	 */
	public void setTaskExecutor(Executor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * 返回此多主机的当前任务执行器
	 */
	protected Executor getTaskExecutor() {
		return this.taskExecutor;
	}

	/**
	 * 将ErrorHandler设置为在侦听器中抛出异常时调用
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * 返回此多主机的当前错误处理程序
	 */
	protected ErrorHandler getErrorHandler() {
		return this.errorHandler;
	}
	
	@Override
	public void multicastEvent(ApplicationEvent event) {
		multicastEvent(event, resolveDefaultEventType(event));
	}

	@Override
	public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
		ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
		// 循环调用监听器
		for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
			if (this.taskExecutor != null) {
				this.taskExecutor.execute(() -> invokeListener(listener, event));
			} else {
				invokeListener(listener, event);
			}
		}
	}

	private ResolvableType resolveDefaultEventType(ApplicationEvent event) {
		return ResolvableType.forClass(event.getClass());
	}
	
	protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
		ErrorHandler errorHandler = getErrorHandler();
		if (errorHandler != null) {
			try {
				doInvokeListener(listener, event);
			} catch (Throwable err) {
				errorHandler.handleError(err);
			}
		} else {
			doInvokeListener(listener, event);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) throws ClassCastException {
		try {
			listener.onApplicationEvent(event);
		} catch (ClassCastException ex) {
//			logger.info(ex.getMessage());
			throw ex;
		}
	}
}

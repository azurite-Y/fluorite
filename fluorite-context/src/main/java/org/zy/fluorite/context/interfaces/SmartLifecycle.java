package org.zy.fluorite.context.interfaces;

/**
 * @DateTime 2020年6月18日 下午4:44:38;
 * @author zy(azurite-Y);
 * @Description 生命周期接口的扩展，
 * 用于在ApplicationContext刷新和/或按特定顺序关闭时需要启动的对象，如与Netty的Socket连接
 */
public interface SmartLifecycle extends Lifecycle, Phased {
	/**
	 * SmartLifecycle的默认阶段：Integer.MAX_值.
	 * <p>
	 * 这不同于与常规生命周期实现相关联的公共阶段0， 它将通常自动启动的SmartLifecycle bean置于稍后的启动阶段和更早的关闭阶段
	 * </p>
	 */
	int DEFAULT_PHASE = Integer.MAX_VALUE;

	/**
	 * 如果容器在刷新包含的ApplicationContext时应自动启动此生命周期组件，则返回true。</br>
	 * 返回false表示组件打算通过显式的start()调用开始，类似于普通的生命周期实现。</br>
	 * 默认实现返回true。
	 */
	default boolean isAutoStartup() {
		return true;
	}

	/**
	 * 指示生命周期组件如果当前正在运行，则必须停止
	 */
	default void stop(Runnable callback) {
		stop();
		callback.run();
	}

	/**
	 * 返回此生命周期对象应在其中运行的阶段。 默认实现返回default_PHASE，以便让stop（）回调在常规生命周期执行之后执行
	 */
	@Override
	default int getPhase() {
		return DEFAULT_PHASE;
	}
}

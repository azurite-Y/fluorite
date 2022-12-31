package org.zy.fluorite.boot.devtools.restart;

import java.beans.Introspector;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.boot.FluoriteApplication;
import org.zy.fluorite.boot.interfaces.FailureHandler;
import org.zy.fluorite.boot.interfaces.FailureHandler.Outcome;
import org.zy.fluorite.boot.interfaces.RestartInitializer;
import org.zy.fluorite.context.interfaces.ConfigurableApplicationContext;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @dateTime 2022年12月27日;
 * @author zy(azurite-Y);
 * @description
 */
public final class Restarter {
	private static Logger logger = LoggerFactory.getLogger(Restarter.class);

	/** 监控实例 */
	private static final Object INSTANCE_MONITOR = new Object();

	private static final String[] NO_ARGS = {};

	private static Restarter instance;

	private final BlockingDeque<LeakSafeThread> leakSafeThreads = new LinkedBlockingDeque<>();

	private final Set<URL> urls = new LinkedHashSet<>();
	
	private final Object monitor = new Object();
	
	private boolean enabled = true;
	
	private URL[] initialUrls;

	private final String mainClassName;

	private final ClassLoader applicationClassLoader;

	private final String[] args;

	private final UncaughtExceptionHandler exceptionHandler;

	private boolean finished = false;

	private final List<ConfigurableApplicationContext> rootContexts = new CopyOnWriteArrayList<>();
	
	private final Lock stopLock = new ReentrantLock();
	
	
	// -------------------------------------------------------------------------------------
	// 构造器
	// -------------------------------------------------------------------------------------
	/**
	 * 用于创建新 {@link Restarter} 实例的内部构造函数
	 * 
	 * @param thread - 源线程
	 * @param args - 应用程序参数
	 * @param initializer - 重新启动初始值设定项
	 * @see #initialize(String[])
	 */
	protected Restarter(Thread thread, String[] args, RestartInitializer initializer) {
		Assert.notNull(thread, "Thread 不能为 null");
		Assert.isTrue(args != null, "Args 不能为 null");
		Assert.notNull(initializer, "Initializer 不能为 null");
		if (Restarter.logger.isDebugEnabled()) {
			Restarter.logger.debug("Creating new Restarter for thread " + thread);
		}
		SilentExitExceptionHandler.setup(thread);
//		this.forceReferenceCleanup = forceReferenceCleanup;
		this.initialUrls = initializer.getInitialUrls(thread);
		this.mainClassName = getMainClassName(thread);
		this.applicationClassLoader = thread.getContextClassLoader();
		this.args = args;
		this.exceptionHandler = thread.getUncaughtExceptionHandler();
		this.leakSafeThreads.add(new LeakSafeThread());
	}

	
	// -------------------------------------------------------------------------------------
	// 实现方法
	// -------------------------------------------------------------------------------------
	private String getMainClassName(Thread thread) {
		try {
			return new MainMethod(thread).getDeclaringClassName();
		}
		catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * 设置是否启用重新启动支持
	 * 
	 * @param enabled - 如果启用了重新启动支持
	 */
	private void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * @return 可用于创建泄漏安全线程的 {@link ThreadFactory}
	 */
	public ThreadFactory getThreadFactory() {
		return new LeakSafeThreadFactory();
	}
	
	/**
	 * 重新启动正在运行的应用程序
	 */
	public void restart() {
		restart(FailureHandler.NONE);
	}

	/**
	 * 重新启动正在运行的应用程序
	 * @param failureHandler - 处理未启动应用程序的失败处理程序
	 */
	public void restart(FailureHandler failureHandler) {
		if (!this.enabled) {
			logger.debug("Application restart is disabled");
			return;
		}
		logger.debug("Restarting application");
		getLeakSafeThread().call(() -> {
			Restarter.this.stop();
			Restarter.this.start(failureHandler);
			return null;
		});
	}

	/**
	 * 启动应用程序
	 * @param failureHandler - 无法启动的应用程序的失败处理程序
	 * @throws Exception - 以防出现错误
	 */
	protected void start(FailureHandler failureHandler) throws Exception {
		do {
			Throwable error = doStart();
			if (error == null) {
				return;
			}
			if (failureHandler.handle(error) == Outcome.ABORT) {
				return;
			}
		}
		while (true);
	}

	private Throwable doStart() throws Exception {
		Assert.notNull(this.mainClassName, "无法找到要重新启动的主类");
		URL[] urls = this.urls.toArray(new URL[0]);
//		ClassLoaderFiles updatedFiles = new ClassLoaderFiles(this.classLoaderFiles);
//		ClassLoader classLoader = new RestartClassLoader(this.applicationClassLoader, urls, updatedFiles, this.logger);
		if (logger.isDebugEnabled()) {
			logger.debug("Starting application " + this.mainClassName + " with URLs " + Arrays.asList(urls));
		}
//		return relaunch(classLoader);
		return relaunch(null);
	}

	/**
	 * 使用指定的类加载器重新启动应用程序
	 * 
	 * @param classLoader - 要使用的类加载器
	 * @return 导致启动失败或为空的任何异常
	 * @throws Exception - 以防出现错误
	 */
	protected Throwable relaunch(ClassLoader classLoader) throws Exception {
		RestartLauncher launcher = new RestartLauncher(classLoader, this.mainClassName, this.args, this.exceptionHandler);
		launcher.start();
		launcher.join();
		return launcher.getError();
	}
	
	/**
	 * 停止应用程序
	 * @throws Exception - 以防出现错误
	 */
	protected void stop() throws Exception {
		logger.debug("Stopping application");
		this.stopLock.lock();
		try {
			for (ConfigurableApplicationContext context : this.rootContexts) {
				context.close();
				this.rootContexts.remove(context);
			}
			cleanupCaches();
		}
		finally {
			this.stopLock.unlock();
		}
		System.gc();
		System.runFinalization();
	}
	
	private void cleanupCaches() throws Exception {
		Introspector.flushCaches();
		ReflectionUtils.clearCache();
	}
	
	/**
	 * 当应用程序日志可用时调用以完成 {@link Restarter} 初始化
	 */
	void finish() {
		synchronized (this.monitor) {
			if (!isFinished()) {
				logger = LoggerFactory.getLogger(Restarter.class);
				this.finished = true;
			}
		}
	}

	boolean isFinished() {
		synchronized (this.monitor) {
			return this.finished;
		}
	}

	
	private LeakSafeThread getLeakSafeThread() {
		try {
			return this.leakSafeThreads.takeFirst();
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(ex);
		}
	}
	
	/**
	 * 初始化重启支持
	 * 
	 * @param args - 主应用程序参数
	 * @see #initialize(String[], boolean, RestartInitializer)
	 * @see #initialize(String[], boolean, RestartInitializer, boolean)
	 */
	public static void initialize(String[] args) {
		initialize(args, new DefaultRestartInitializer());
	}

	/**
	 * 初始化重启支持
	 * 
	 * @param args - 主要应用参数
	 * @param initializer - 重启初始值设定项
	 * @see #initialize(String[], boolean, RestartInitializer)
	 * @see #initialize(String[], boolean, RestartInitializer, boolean)
	 */
	public static void initialize(String[] args, RestartInitializer initializer) {
		initialize(args, initializer, true);
	}

	/**
	 * 初始化当前应用程序的重新启动支持。
	 * 由 {@link RestartApplicationListener} 自动调用，但如果主要应用程序参数与传递给 {@link FluoriteApplication} 的参数不相同，也可以直接调用。
	 * 
	 * @param args - 主要应用参数
	 * @param initializer - 重启初始值设定项
	 * @param restartOnInitialize - 当 {@link RestartInitializer} 返回非空结果时，是否应该立即重新启动restarter
	 */
	public static void initialize(String[] args, RestartInitializer initializer, boolean restartOnInitialize) {
		Restarter localInstance = null;
		synchronized (INSTANCE_MONITOR) {
			if (instance == null) {
				localInstance = new Restarter(Thread.currentThread(), args, initializer);
				instance = localInstance;
			}
		}
		if (localInstance != null) {
			localInstance.initialize(restartOnInitialize);
		}
	}

	protected void initialize(boolean restartOnInitialize) {
		if (this.initialUrls != null) {
			this.urls.addAll(Arrays.asList(this.initialUrls));
			if (restartOnInitialize) {
				if (logger.isDebugEnabled()) {
					logger.debug("Immediately restarting application");
				} 
				immediateRestart();
			}
		}
	}
	
	/**
	 * 立即重启
	 */
	private void immediateRestart() {
		try {
			getLeakSafeThread().callAndWait(() -> {
				start(FailureHandler.NONE);
				cleanupCaches();
				return null;
			});
		}
		catch (Exception ex) {
			logger.warn("Unable to initialize restarter", ex);
		}
		SilentExitExceptionHandler.exitCurrentThread();
	}
	
	/**
	 * 返回活动的 {@link Restarter} 实例。在 {@link #initialize(String[]) 初始化} 之前不能调用。
	 * 
	 * @return 重新启动的
	 */
	public static Restarter getInstance() {
		synchronized (INSTANCE_MONITOR) {
			Assert.isTrue(instance != null, "Restarter has not been initialized");
			return instance;
		}
	}
	
	/**
	 * 设置restarter实例(对测试有用)
	 * @param instance - 要设置的实例
	 */
	static void setInstance(Restarter instance) {
		synchronized (INSTANCE_MONITOR) {
			Restarter.instance = instance;
		}
	}
	
	/**
	 * @return {@link RestartInitializer} 配置的初始URL集或 {@code null}
	 */
	public URL[] getInitialUrls() {
		return this.initialUrls;
	}
	
	/**
	 * 初始化并禁用重启支持
	 */
	public static void disable() {
		initialize(NO_ARGS, RestartInitializer.NONE);
		getInstance().setEnabled(false);
	}

	/**
	 * 清除实例。主要为测试提供，通常不用于应用程序代码。
	 */
	public static void clearInstance() {
		synchronized (INSTANCE_MONITOR) {
			instance = null;
		}
	}
	
	void prepare(ConfigurableApplicationContext applicationContext) {
		if (applicationContext != null && applicationContext.getParent() != null) {
			return;
		}
		this.rootContexts.add(applicationContext);
	}

	void remove(ConfigurableApplicationContext applicationContext) {
		if (applicationContext != null) {
			this.rootContexts.remove(applicationContext);
		}
	}
	
	// -------------------------------------------------------------------------------------
	// 内部类
	// -------------------------------------------------------------------------------------
	/**
	 * 早期创建的线程，以便不保留｛@link RestartClassLoader｝
	 */
	private class LeakSafeThread extends Thread {
		private Callable<?> callable;

		private Object result;

		LeakSafeThread() {
			setDaemon(false);
		}

		void call(Callable<?> callable) {
			this.callable = callable;
			start();
		}

		@SuppressWarnings("unchecked")
		<V> V callAndWait(Callable<V> callable) {
			this.callable = callable;
			start();
			try {
				join();
				return (V) this.result;
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(ex);
			}
		}

		@Override
		public void run() {
			try {
				Restarter.this.leakSafeThreads.put(new LeakSafeThread());
				this.result = this.callable.call();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}

	}

	/**
	 * 创建泄漏安全线程的 {@link ThreadFactory}
	 */
	private class LeakSafeThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable runnable) {
			return getLeakSafeThread().callAndWait(() -> {
				Thread thread = new Thread(runnable);
				thread.setContextClassLoader(Restarter.this.applicationClassLoader);
				return thread;
			});
		}

	}
}

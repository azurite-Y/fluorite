package org.zy.fluorite.boot.devtools.classpath;

import java.net.URL;

import org.zy.fluorite.boot.devtools.filewatch.FileSystemWatcher;
import org.zy.fluorite.boot.interfaces.ClassPathRestartStrategy;
import org.zy.fluorite.context.interfaces.ApplicationContext;
import org.zy.fluorite.context.interfaces.aware.ApplicationContextAware;
import org.zy.fluorite.core.exception.BeansException;
import org.zy.fluorite.core.interfaces.instantiation.DisposableBean;
import org.zy.fluorite.core.interfaces.instantiation.InitializingBean;
import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月23日;
 * @author zy(azurite-Y);
 * @description 封装 {@link FileSystemWatcher} 以监视本地类路径目录的更改
 */
public class ClassPathFileSystemWatcher implements InitializingBean, DisposableBean, ApplicationContextAware {

	private final FileSystemWatcher fileSystemWatcher;

	private ClassPathRestartStrategy restartStrategy;

	private ApplicationContext applicationContext;

	/** 重启时停止监控器 */
	private boolean stopWatcherOnRestart;

	/**
	 * 创建一个新的 {@link ClassPathFileSystemWatcher} 实例
	 * 
	 * @param fileSystemWatcherFactory - 创建用于监视本地文件系统的底层 {@link FileSystemWatcher} 的工厂
	 * @param restartStrategy - 类路径重启策略
	 * @param urls - 要监视的URL
	 */
	public ClassPathFileSystemWatcher(FileSystemWatcher fileSystemWatcher, ClassPathRestartStrategy restartStrategy, URL[] urls) {
		Assert.notNull(fileSystemWatcher, "FileSystemWatcherFactory 不能为 null");
		Assert.notNull(urls, "Urls 不能为 null");
		this.fileSystemWatcher = fileSystemWatcher;
		this.restartStrategy = restartStrategy;
		this.fileSystemWatcher.addSourceDirectories(new ClassPathDirectories(urls));
	}

	/**
	 * 设置在完全重新启动时是否应停止 {@link FileSystemWatcher}
	 * 
	 * @param stopWatcherOnRestart - 重新启动时是否应停止 {@link FileSystemWatcher}
	 */
	public void setStopWatcherOnRestart(boolean stopWatcherOnRestart) {
		this.stopWatcherOnRestart = stopWatcherOnRestart;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.restartStrategy != null) {
			FileSystemWatcher watcherToStop = null;
			if (this.stopWatcherOnRestart) {
				watcherToStop = this.fileSystemWatcher;
			}
			this.fileSystemWatcher.addListener(new ClassPathFileChangeListener(this.applicationContext, this.restartStrategy, watcherToStop));
		}
		this.fileSystemWatcher.start();
	}

	@Override
	
	public void destroy() throws Exception {
		this.fileSystemWatcher.stop();
	}

}

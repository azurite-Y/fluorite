package org.zy.fluorite.boot.devtools.classpath;

import java.util.Set;

import org.zy.fluorite.boot.devtools.filewatch.ChangedFile;
import org.zy.fluorite.boot.devtools.filewatch.ChangedFiles;
import org.zy.fluorite.boot.devtools.filewatch.ClassPathFileChangedEvent;
import org.zy.fluorite.boot.devtools.filewatch.FileSystemWatcher;
import org.zy.fluorite.boot.interfaces.ClassPathRestartStrategy;
import org.zy.fluorite.boot.interfaces.FileChangeListener;
import org.zy.fluorite.context.event.interfaces.ApplicationEventPublisher;
import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月27日;
 * @author zy(azurite-Y);
 * @description 一个发布{@link ClassPathFileChangedEvent }s 的 {@link FileChangeListener}。
 */
class ClassPathFileChangeListener implements FileChangeListener {

	private final ApplicationEventPublisher eventPublisher;

	private final ClassPathRestartStrategy restartStrategy;

	private final FileSystemWatcher fileSystemWatcherToStop;

	/**
	 * 创建一个新的 {@link ClassPathFileChangeListener} 实例。
	 * 
	 * @param eventPublisher - 事件发布者使用发送事件
	 * @param restartStrategy - 要使用的重启策略
	 * @param fileSystemWatcherToStop - 文件系统监视程序在重新启动时停止(或为空)
	 */
	ClassPathFileChangeListener(ApplicationEventPublisher eventPublisher, ClassPathRestartStrategy restartStrategy, FileSystemWatcher fileSystemWatcherToStop) {
		Assert.notNull(eventPublisher, "EventPublisher 不能为 null");
		Assert.notNull(restartStrategy, "RestartStrategy 不能为 null");
		this.eventPublisher = eventPublisher;
		this.restartStrategy = restartStrategy;
		this.fileSystemWatcherToStop = fileSystemWatcherToStop;
	}

	@Override
	public void onChange(Set<ChangedFiles> changeSet) {
		boolean restart = isRestartRequired(changeSet);
		publishEvent(new ClassPathFileChangedEvent(this, changeSet, restart));
	}

	private void publishEvent(ClassPathFileChangedEvent event) {
		this.eventPublisher.publishEvent(event);
		if (event.isRestartRequired() && this.fileSystemWatcherToStop != null) {
			this.fileSystemWatcherToStop.stop();
		}
	}

	private boolean isRestartRequired(Set<ChangedFiles> changeSet) {
		for (ChangedFiles changedFiles : changeSet) {
			for (ChangedFile changedFile : changedFiles) {
				if (this.restartStrategy.isRestartRequired(changedFile)) {
					return true;
				}
			}
		}
		return false;
	}

}

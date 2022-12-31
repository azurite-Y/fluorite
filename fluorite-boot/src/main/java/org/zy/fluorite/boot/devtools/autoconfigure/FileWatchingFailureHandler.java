package org.zy.fluorite.boot.devtools.autoconfigure;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.zy.fluorite.boot.devtools.classpath.ClassPathDirectories;
import org.zy.fluorite.boot.devtools.filewatch.ChangedFiles;
import org.zy.fluorite.boot.devtools.filewatch.FileSystemWatcher;
import org.zy.fluorite.boot.devtools.restart.Restarter;
import org.zy.fluorite.boot.interfaces.FailureHandler;
import org.zy.fluorite.boot.interfaces.FileChangeListener;

/**
 * @dateTime 2022年12月24日;
 * @author zy(azurite-Y);
 * @description
 */
public class FileWatchingFailureHandler implements FailureHandler {

	private final FileSystemWatcher fileSystemWatcher;

	public FileWatchingFailureHandler(FileSystemWatcher fileSystemWatcher) {
		this.fileSystemWatcher = fileSystemWatcher;
	}

	@Override
	public Outcome handle(Throwable failure) {
		CountDownLatch latch = new CountDownLatch(1);
		fileSystemWatcher.addSourceDirectories(new ClassPathDirectories(Restarter.getInstance().getInitialUrls()));
		fileSystemWatcher.addListener(new Listener(latch));
		fileSystemWatcher.start();
		
		try {
			latch.await();
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		return Outcome.RETRY;
	}

	private static class Listener implements FileChangeListener {
		private final CountDownLatch latch;

		Listener(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void onChange(Set<ChangedFiles> changeSet) {
			this.latch.countDown();
		}
	}
}

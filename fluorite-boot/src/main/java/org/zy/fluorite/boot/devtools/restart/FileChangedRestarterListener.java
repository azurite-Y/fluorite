package org.zy.fluorite.boot.devtools.restart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.boot.devtools.autoconfigure.FileWatchingFailureHandler;
import org.zy.fluorite.boot.devtools.filewatch.ClassPathFileChangedEvent;
import org.zy.fluorite.boot.devtools.filewatch.FileSystemWatcher;
import org.zy.fluorite.context.event.interfaces.ApplicationListener;

/**
 * @dateTime 2022年12月28日;
 * @author zy(azurite-Y);
 * @description
 */
public class FileChangedRestarterListener implements ApplicationListener<ClassPathFileChangedEvent> {
	private Logger logger = LoggerFactory.getLogger(FileChangedRestarterListener.class);

	private FileSystemWatcher fileSystemWatcher;
	
	public FileChangedRestarterListener(FileSystemWatcher fileSystemWatcher) {
		super();
		this.fileSystemWatcher = fileSystemWatcher;
	}

	@Override
	public void onApplicationEvent(ClassPathFileChangedEvent event) {
		if (event.isRestartRequired()) {
			if (logger.isDebugEnabled()) {
				logger.debug("ClassPath File Changed. ChangeSet: {}", event.getChangeSet());
			}
			
			Restarter.getInstance().restart(new FileWatchingFailureHandler(fileSystemWatcher));
		}
	}
}

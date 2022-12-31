package org.zy.fluorite.boot.devtools.autoconfigure;

import java.net.URL;

import org.zy.fluorite.boot.devtools.autoconfigure.DevToolsProperties.Restart;
import org.zy.fluorite.boot.devtools.classpath.ClassPathFileSystemWatcher;
import org.zy.fluorite.boot.devtools.classpath.PatternClassPathRestartStrategy;
import org.zy.fluorite.boot.devtools.filewatch.ClassPathFileChangedEvent;
import org.zy.fluorite.boot.devtools.filewatch.FileSystemWatcher;
import org.zy.fluorite.boot.devtools.restart.FileChangedRestarterListener;
import org.zy.fluorite.boot.devtools.restart.Restarter;
import org.zy.fluorite.boot.interfaces.ClassPathRestartStrategy;
import org.zy.fluorite.context.annotation.EnableConfigurationProperties;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnProperty;
import org.zy.fluorite.context.event.interfaces.ApplicationListener;
import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.annotation.Configuration;

/**
 * @dateTime 2022年12月23日;
 * @author zy(azurite-Y);
 * @description
 */
@Configuration
@EnableConfigurationProperties(DevToolsProperties.class)
@ConditionalOnProperty(prefix = "fluorite.devtools.restart", value = "enabled", havingValue = "true", matchIfMissing = true)
public class RestartConfiguration {
	
	@Bean
	ApplicationListener<ClassPathFileChangedEvent> fileChangedRestarterListener(FileSystemWatcher fileSystemWatcher) {
		// 方便传递泛型参数
		return new FileChangedRestarterListener(fileSystemWatcher); 
				
//		return (event) -> {
//			if (event.isRestartRequired()) {
//				Restarter.getInstance().restart(new FileWatchingFailureHandler(fileSystemWatcher));
//			}
//		};
	}
	
	@Bean
	ClassPathRestartStrategy classPathRestartStrategy(DevToolsProperties devToolsProperties) {
		return new PatternClassPathRestartStrategy(devToolsProperties.getRestart().getAllExclude());
	}
	
	@Bean
	ClassPathFileSystemWatcher classPathFileSystemWatcher(FileSystemWatcher fileSystemWatcher, ClassPathRestartStrategy classPathRestartStrategy) {
		URL[] urls = Restarter.getInstance().getInitialUrls();
		ClassPathFileSystemWatcher watcher = new ClassPathFileSystemWatcher(fileSystemWatcher, classPathRestartStrategy, urls);
		watcher.setStopWatcherOnRestart(true);
		return watcher;
	}
	
	@Bean
	public FileSystemWatcher fileSystemWatcher(DevToolsProperties properties) {
		Restart restartProperties = properties.getRestart();
		FileSystemWatcher watcher = new FileSystemWatcher(true, restartProperties.getPollInterval(), restartProperties.getQuietPeriod());
		return watcher;
	}
}

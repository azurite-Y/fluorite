package org.zy.fluorite.boot.interfaces;

import org.zy.fluorite.boot.devtools.filewatch.ChangedFile;

/**
 * @dateTime 2022年12月23日;
 * @author zy(azurite-Y);
 * @description 策略接口，用于确定更改的类路径文件何时应该触发整个应用程序重新启动。例如，静态web资源可能不像类文件那样需要完全重启。
 */
@FunctionalInterface
public interface ClassPathRestartStrategy {
	
	/**
	 * 如果需要完全重启，则返回true
	 * 
	 * @param file - 更改后的文件
	 * @return 如果需要完全重启，则为 {@code true}
	 */
	boolean isRestartRequired(ChangedFile file);
	
}

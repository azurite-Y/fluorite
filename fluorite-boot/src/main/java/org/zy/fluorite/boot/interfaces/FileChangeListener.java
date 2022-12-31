package org.zy.fluorite.boot.interfaces;

import java.util.Set;

import org.zy.fluorite.boot.devtools.filewatch.ChangedFiles;

/**
 * @dateTime 2022年12月24日;
 * @author zy(azurite-Y);
 * @description 检测到文件更改时的回调接口
 */
@FunctionalInterface
public interface FileChangeListener {

	/**
	 * 当文件已更改时调用
	 * 
	 * @param changeSet - 一组 {@link ChangedFiles}
	 */
	void onChange(Set<ChangedFiles> changeSet);

}

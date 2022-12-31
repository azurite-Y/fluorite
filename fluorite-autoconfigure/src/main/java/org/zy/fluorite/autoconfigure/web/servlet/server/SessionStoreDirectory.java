package org.zy.fluorite.autoconfigure.web.servlet.server;

import java.io.File;

import org.zy.fluorite.core.utils.ApplicationTemp;
import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2021年12月24日;
 * @author zy(azurite-Y);
 * @description
 */
public class SessionStoreDirectory {
	private File directory;

	File getDirectory() {
		return this.directory;
	}

	void setDirectory(File directory) {
		this.directory = directory;
	}

	public File getValidDirectory(boolean mkdirs) {
		File dir = getDirectory();
		if (dir == null) {
			return new ApplicationTemp().getDir("servlet-sessions");
		}
		if (!dir.isAbsolute()) {
			dir = dir.getAbsoluteFile();
		}
		if (!dir.exists() && mkdirs) {
			dir.mkdirs();
		}
		assertDirectory(mkdirs, dir);
		return dir;
	}

	private void assertDirectory(boolean mkdirs, File dir) {
		Assert.isTrue(!mkdirs || dir.exists(), () -> "Session目录" + dir + "不存在");
		Assert.isTrue(!dir.isFile(), () -> "Session目录" + dir + "指向一个文件");
	}
}

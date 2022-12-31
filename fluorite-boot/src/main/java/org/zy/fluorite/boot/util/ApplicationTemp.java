package org.zy.fluorite.boot.util;

import java.io.File;
import java.util.UUID;

import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description 提供对应用程序特定的临时目录的访问。
 */
public class ApplicationTemp {

	private volatile File dir;
	
	/**
	 * 创建一个新的 {@link ApplicationTemp} 实例。
	 */
	public ApplicationTemp() {}

	/**
	 * 返回应用程序临时的子目录
	 * 
	 * @param subDir - 子目录名
	 * @return 一个目录
	 */
	public File getDir(String subDir) {
		File dir = new File(getDir(), subDir);
		dir.mkdirs();
		return dir;
	}

	/**
	 * 返回用于应用程序特定的临时文件的目录
	 * 
	 * @return 应用程序临时目录
	 */
	public File getDir() {
		if (this.dir == null) {
			synchronized (this) {
				this.dir = new File(getTempDirectory(), UUID.randomUUID().toString());
				this.dir.mkdirs();
				Assert.isTrue(this.dir.exists(), () -> "Unable to create temp directory " + this.dir);
			}
		}
		return this.dir;
	}

	private File getTempDirectory() {
		String property = System.getProperty("java.io.tmpdir");
		Assert.isTrue(Assert.hasText(property), "无 'java.io.tmpdir' 属性设置");
		File file = new File(property);
		Assert.isTrue(file.exists(), () -> "临时目录 " + file + " 不存在");
		Assert.isTrue(file.isDirectory(), () -> "临时位置 " + file + " 不是一个目录");
		return file;
	}
}

package org.zy.fluorite.boot.devtools.filewatch;

import java.io.File;

import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月27日;
 * @author zy(azurite-Y);
 * @description 文件在给定时间点的快照
 */
public class FileSnapshot {
	private final File file;

	private final boolean exists;

	private final long length;

	private final long lastModified;

	FileSnapshot(File file) {
		Assert.notNull(file, "File 不能为 null");
		Assert.isTrue(file.isFile() || !file.exists(), "File 不能为一个 directory");
		this.file = file;
		this.exists = file.exists();
		this.length = file.length();
		this.lastModified = file.lastModified();
	}

	File getFile() {
		return this.file;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof FileSnapshot) {
			FileSnapshot other = (FileSnapshot) obj;
			boolean equals = this.file.equals(other.file);
			equals = equals && this.exists == other.exists;
			equals = equals && this.length == other.length;
			equals = equals && this.lastModified == other.lastModified;
			return equals;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		int hashCode = this.file.hashCode();
		hashCode = 31 * hashCode + Boolean.hashCode(this.exists);
		hashCode = 31 * hashCode + Long.hashCode(this.length);
		hashCode = 31 * hashCode + Long.hashCode(this.lastModified);
		return hashCode;
	}

	@Override
	public String toString() {
		return this.file.toString();
	}
}

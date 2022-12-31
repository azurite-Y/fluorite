package org.zy.fluorite.boot.devtools.filewatch;

import java.io.File;
import java.io.IOException;

import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月23日;
 * @author zy(azurite-Y);
 * @description 已更改的单个文件
 */
public class ChangedFile {
	private final File sourceDirectory;
	private final File file;
	private final Type type;

	/**
	 * 创建一个新的 {@link ChangedFile} 实例.
	 * 
	 * @param sourceDirectory - 源目录
	 * @param file -  the file
	 * @param type - 变化的类型
	 */
	public ChangedFile(File sourceDirectory, File file, Type type) {
		Assert.notNull(sourceDirectory, "SourceDirectory 不能为 null");
		Assert.notNull(file, "File 不能为 null");
		Assert.notNull(type, "Type must not be null");
		this.sourceDirectory = sourceDirectory;
		this.file = file;
		this.type = type;
	}

	/**
	 * @return 已更改的文件
	 */
	public File getFile() {
		return this.file;
	}

	/**
	 * @return 更改的类型
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * 返回相对于源目录的文件名
	 * @return 相对名称
	 */
	public String getRelativeName() {
		File directory = this.sourceDirectory.getAbsoluteFile();
		File file = this.file.getAbsoluteFile();
		
		String directoryName = null;
		String fileName = null;
		try {
			directoryName = directory.getCanonicalPath();
			fileName = file.getCanonicalPath();
			
			Assert.isTrue(fileName.startsWith(directoryName), "源目录[" + directoryName + "]路径中不包含该文件路径[" + fileName + "]");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileName.substring(directoryName.length() + 1).replace('\\', '/');
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof ChangedFile) {
			ChangedFile other = (ChangedFile) obj;
			return this.file.equals(other.file) && this.type.equals(other.type);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return this.file.hashCode() * 31 + this.type.hashCode();
	}

	@Override
	public String toString() {
		return this.file + " (" + this.type + ")";
	}

	/**
	 * Change types.
	 */
	public enum Type {
		/** 已添加新文件 */
		ADD,

		/** 现有文件已被修改 */
		MODIFY,

		/** 已有文件被删除 */
		DELETE
	}
}

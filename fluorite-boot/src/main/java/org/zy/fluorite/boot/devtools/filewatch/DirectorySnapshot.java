package org.zy.fluorite.boot.devtools.filewatch;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.zy.fluorite.boot.devtools.filewatch.ChangedFile.Type;
import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月27日;
 * @author zy(azurite-Y);
 * @description 目录在给定时间点的快照
 */
public class DirectorySnapshot {
	private static final Set<String> DOTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(".", "..")));

	private final File directory;

	private final Date time;

	private Set<FileSnapshot> files;

	/**
	 * 使用给定的目录创建一个新的 {@link DirectorySnapshot}.
	 * @param directory - 源目录
	 */
	DirectorySnapshot(File directory) {
		Assert.notNull(directory, "Directory 不能为 null");
		Assert.isTrue(!directory.isFile(), () -> "Directory '" + directory + "' 不能是一个 file");
		this.directory = directory;
		this.time = new Date();
		Set<FileSnapshot> files = new LinkedHashSet<>();
		collectFiles(directory, files);
		this.files = Collections.unmodifiableSet(files);
	}

	private void collectFiles(File source, Set<FileSnapshot> result) {
		File[] children = source.listFiles();
		if (children != null) {
			for (File child : children) {
				if (child.isDirectory() && !DOTS.contains(child.getName())) {
					collectFiles(child, result);
				}
				else if (child.isFile()) {
					result.add(new FileSnapshot(child));
				}
			}
		}
	}

	ChangedFiles getChangedFiles(DirectorySnapshot snapshot, FileFilter triggerFilter) {
		Assert.notNull(snapshot, "Snapshot 不能为 null");
		File directory = this.directory;
		Assert.isTrue(snapshot.directory.equals(directory), "Snapshot source directory 需为 '" + directory + "'");
		Set<ChangedFile> changes = new LinkedHashSet<>();
		Map<File, FileSnapshot> previousFiles = getFilesMap();
		for (FileSnapshot currentFile : snapshot.files) {
			if (acceptChangedFile(triggerFilter, currentFile)) {
				FileSnapshot previousFile = previousFiles.remove(currentFile.getFile());
				if (previousFile == null) {
					changes.add(new ChangedFile(directory, currentFile.getFile(), Type.ADD));
				}
				else if (!previousFile.equals(currentFile)) {
					changes.add(new ChangedFile(directory, currentFile.getFile(), Type.MODIFY));
				}
			}
		}
		for (FileSnapshot previousFile : previousFiles.values()) {
			if (acceptChangedFile(triggerFilter, previousFile)) {
				changes.add(new ChangedFile(directory, previousFile.getFile(), Type.DELETE));
			}
		}
		return new ChangedFiles(directory, changes);
	}

	private boolean acceptChangedFile(FileFilter triggerFilter, FileSnapshot file) {
		return (triggerFilter == null || !triggerFilter.accept(file.getFile()));
	}

	private Map<File, FileSnapshot> getFilesMap() {
		Map<File, FileSnapshot> files = new LinkedHashMap<>();
		for (FileSnapshot file : this.files) {
			files.put(file.getFile(), file);
		}
		return files;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof DirectorySnapshot) {
			return equals((DirectorySnapshot) obj, null);
		}
		return super.equals(obj);
	}

	boolean equals(DirectorySnapshot other, FileFilter filter) {
		if (this.directory.equals(other.directory)) {
			Set<FileSnapshot> ourFiles = filter(this.files, filter);
			Set<FileSnapshot> otherFiles = filter(other.files, filter);
			return ourFiles.equals(otherFiles);
		}
		return false;
	}

	private Set<FileSnapshot> filter(Set<FileSnapshot> source, FileFilter filter) {
		if (filter == null) {
			return source;
		}
		Set<FileSnapshot> filtered = new LinkedHashSet<>();
		for (FileSnapshot file : source) {
			if (filter.accept(file.getFile())) {
				filtered.add(file);
			}
		}
		return filtered;
	}

	@Override
	public int hashCode() {
		int hashCode = this.directory.hashCode();
		hashCode = 31 * hashCode + this.files.hashCode();
		return hashCode;
	}

	/**
	 * 返回此快照的源目录
	 * @return 源目录
	 */
	File getDirectory() {
		return this.directory;
	}

	@Override
	public String toString() {
		return this.directory + " snapshot at " + this.time;
	}
}

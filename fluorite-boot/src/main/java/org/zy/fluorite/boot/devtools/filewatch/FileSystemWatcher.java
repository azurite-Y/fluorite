package org.zy.fluorite.boot.devtools.filewatch;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.zy.fluorite.boot.interfaces.FileChangeListener;
import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月27日;
 * @author zy(azurite-Y);
 * @description
 */
public class FileSystemWatcher {
	private static final int DEFAULT_POLL_INTERVAL = 1000;

	private static final int DEFAULT_QUIET_PERIOD = 400;

	private final List<FileChangeListener> listeners = new ArrayList<>();

	/** 监视线程是否作为守护线程 */
	private final boolean daemon;

	/** 轮询类路径更改之间等待的时间量, 单位为ms */
	private final long pollInterval;

	/** 触发重新启动之前不需要任何类路径更改的安静时间, 单位为ms */
	private final long quietPeriod;

	/** 剩余扫描数 */
	private final AtomicInteger remainingScans = new AtomicInteger(-1);

	private final Map<File, DirectorySnapshot> directories = new HashMap<>();

	private Thread watchThread;

	private FileFilter triggerFilter;

	private final Object monitor = new Object();
	
	
	/**
	 * 创建一个新的 {@link FileSystemWatcher} 实例.
	 */
	public FileSystemWatcher() {
		this(true, DEFAULT_POLL_INTERVAL, DEFAULT_QUIET_PERIOD);
	}

	/**
	 * 创建一个新的 {@link FileSystemWatcher} 实例。
	 * 
	 * @param daemon - 如果用于监视发生变化的进程是守护线程
	 * @param pollInterval - 检查更改之间等待的时间
	 * @param quietPeriod - 检测到更改后确保更新完成所需的时间
	 */
	public FileSystemWatcher(boolean daemon, int pollInterval, int quietPeriod) {
		Assert.notNull(pollInterval, "PollInterval 不能为 null");
		Assert.notNull(quietPeriod, "QuietPeriod 不能为 null");
		Assert.isTrue(pollInterval > 0, "PollInterval 必须为正数");
		Assert.isTrue(quietPeriod > 0, "QuietPeriod 必须为正数");
		Assert.isTrue(pollInterval > quietPeriod, "PollInterval 必须大于 QuietPeriod");
		this.pollInterval = pollInterval;
		this.quietPeriod = quietPeriod;
		this.daemon = daemon;
	}
	
	/**
	 * 为文件更改事件添加侦听器。{@link #start() 启动之后}观察程序后无法调用。
	 * 
	 * @param fileChangeListener - 添加的Listener
	 */
	public void addListener(FileChangeListener fileChangeListener) {
		Assert.notNull(fileChangeListener, "FileChangeListener 不能为 null");
		synchronized (this.monitor) {
			checkNotStarted();
			this.listeners.add(fileChangeListener);
		}
	}

	/**
	 * 添加要监视的源目录。不能在监视程序 {@link #start() 启动后} 调用。
	 * 
	 * @param directories - 要监视的源目录
	 */
	public void addSourceDirectories(Iterable<File> directories) {
		Assert.notNull(directories, "Directories 不能为 null");
		synchronized (this.monitor) {
			directories.forEach(this::addSourceDirectory);
		}
	}

	/**
	 * 添加要监视的源目录。不能在监视程序 {@link #start() 启动后} 调用。
	 * 
	 * @param directory - 要监视的源目录
	 */
	public void addSourceDirectory(File directory) {
		Assert.notNull(directory, "Directory 不能为 null");
		Assert.isTrue(!directory.isFile(), () -> "Directory '" + directory + "' 不能是一个 file");
		synchronized (this.monitor) {
			checkNotStarted();
			this.directories.put(directory, null);
		}
	}

	/**
	 * 设置一个可选的 {@link FileFilter}，用于限制触发更改的文件。
	 * @param triggerFilter - 触发筛选器或null
	 */
	public void setTriggerFilter(FileFilter triggerFilter) {
		synchronized (this.monitor) {
			this.triggerFilter = triggerFilter;
		}
	}

	private void checkNotStarted() {
		synchronized (this.monitor) {
			Assert.isTrue(this.watchThread == null, "FileSystemWatcher already started");
		}
	}

	/**
	 * 开始监视源目录中的更改。
	 */
	public void start() {
		synchronized (this.monitor) {
			saveInitialSnapshots();
			if (this.watchThread == null) {
				Map<File, DirectorySnapshot> localDirectories = new HashMap<>(this.directories);
				this.watchThread = new Thread(
						new Watcher(this.remainingScans, new ArrayList<>(this.listeners), this.triggerFilter, this.pollInterval, this.quietPeriod, localDirectories));
				this.watchThread.setName("File Watcher");
				this.watchThread.setDaemon(this.daemon);
				this.watchThread.start();
			}
		}
	}

	private void saveInitialSnapshots() {
		this.directories.replaceAll((f, v) -> new DirectorySnapshot(f));
	}

	/** 
	 * 停止监控源目录。
	 */
	public void stop() {
		stopAfter(0);
	}
	
	/**
	 * 停止监控源目录
	 * 
	 * @param remainingScans - 剩余的扫描数
	 */
	void stopAfter(int remainingScans) {
		Thread thread;
		synchronized (this.monitor) {
			thread = this.watchThread;
			if (thread != null) {
				this.remainingScans.set(remainingScans);
				if (remainingScans <= 0) {
					thread.interrupt();
				}
			}
			this.watchThread = null;
		}
		if (thread != null && Thread.currentThread() != thread) {
			try {
				thread.join();
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private static final class Watcher implements Runnable {
		private final AtomicInteger remainingScans;

		private final List<FileChangeListener> listeners;

		private final FileFilter triggerFilter;
		
		/** 轮询类路径更改之间等待的时间量, 单位为ms */
		private final long pollInterval;

		/** 触发重新启动之前不需要任何类路径更改的安静时间, 单位为ms */
		private final long quietPeriod;

		private Map<File, DirectorySnapshot> directories;

		private Watcher(AtomicInteger remainingScans, List<FileChangeListener> listeners, FileFilter triggerFilter,
				long pollInterval, long quietPeriod, Map<File, DirectorySnapshot> directories) {
			this.remainingScans = remainingScans;
			this.listeners = listeners;
			this.triggerFilter = triggerFilter;
			this.pollInterval = pollInterval;
			this.quietPeriod = quietPeriod;
			this.directories = directories;
		}

		@Override
		public void run() {
			int remainingScans = this.remainingScans.get();
			while (remainingScans > 0 || remainingScans == -1) {
				try {
					if (remainingScans > 0) {
						this.remainingScans.decrementAndGet();
					}
					scan();
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				remainingScans = this.remainingScans.get();
			}
		}

		private void scan() throws InterruptedException {
			Thread.sleep(this.pollInterval - this.quietPeriod);
			Map<File, DirectorySnapshot> previous;
			Map<File, DirectorySnapshot> current = this.directories;
			do {
				previous = current;
				current = getCurrentSnapshots();
				Thread.sleep(this.quietPeriod);
			}
			while (isDifferent(previous, current));
			if (isDifferent(this.directories, current)) {
				updateSnapshots(current.values());
			}
		}

		private boolean isDifferent(Map<File, DirectorySnapshot> previous, Map<File, DirectorySnapshot> current) {
			if (!previous.keySet().equals(current.keySet())) {
				return true;
			}
			for (Map.Entry<File, DirectorySnapshot> entry : previous.entrySet()) {
				DirectorySnapshot previousDirectory = entry.getValue();
				DirectorySnapshot currentDirectory = current.get(entry.getKey());
				if (!previousDirectory.equals(currentDirectory, this.triggerFilter)) {
					return true;
				}
			}
			return false;
		}

		private Map<File, DirectorySnapshot> getCurrentSnapshots() {
			Map<File, DirectorySnapshot> snapshots = new LinkedHashMap<>();
			for (File directory : this.directories.keySet()) {
				snapshots.put(directory, new DirectorySnapshot(directory));
			}
			return snapshots;
		}

		private void updateSnapshots(Collection<DirectorySnapshot> snapshots) {
			Map<File, DirectorySnapshot> updated = new LinkedHashMap<>();
			Set<ChangedFiles> changeSet = new LinkedHashSet<>();
			for (DirectorySnapshot snapshot : snapshots) {
				DirectorySnapshot previous = this.directories.get(snapshot.getDirectory());
				updated.put(snapshot.getDirectory(), snapshot);
				ChangedFiles changedFiles = previous.getChangedFiles(snapshot, this.triggerFilter);
				if (!changedFiles.getFiles().isEmpty()) {
					changeSet.add(changedFiles);
				}
			}
			if (!changeSet.isEmpty()) {
				fireListeners(Collections.unmodifiableSet(changeSet));
			}
			this.directories = updated;
		}

		private void fireListeners(Set<ChangedFiles> changeSet) {
			for (FileChangeListener listener : this.listeners) {
				listener.onChange(changeSet);
			}
		}

	}
}

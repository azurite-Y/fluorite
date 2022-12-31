package org.zy.fluorite.boot.devtools.restart;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;

/**
 * @dateTime 2022年12月27日;
 * @author zy(azurite-Y);
 * @description UnaughtExceptionHandler修饰符，允许线程以静默方式退出。
 */
class SilentExitExceptionHandler implements UncaughtExceptionHandler {
	private final UncaughtExceptionHandler delegate;

	SilentExitExceptionHandler(UncaughtExceptionHandler delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable exception) {
		if (exception instanceof SilentExitException) {
			if (isJvmExiting(thread)) {
				preventNonZeroExitCode();
			}
			return;
		}
		if (this.delegate != null) {
			this.delegate.uncaughtException(thread, exception);
		}
	}
	
	private boolean isJvmExiting(Thread exceptionThread) {
		for (Thread thread : getAllThreads()) {
			if (thread != exceptionThread && thread.isAlive() && !thread.isDaemon()) {
				return false;
			}
		}
		return true;
	}
	
	protected Thread[] getAllThreads() {
		ThreadGroup rootThreadGroup = getRootThreadGroup();
		Thread[] threads = new Thread[32];
		int count = rootThreadGroup.enumerate(threads);
		while (count == threads.length) {
			threads = new Thread[threads.length * 2];
			count = rootThreadGroup.enumerate(threads);
		}
		return Arrays.copyOf(threads, count);
	}

	private ThreadGroup getRootThreadGroup() {
		ThreadGroup candidate = Thread.currentThread().getThreadGroup();
		while (candidate.getParent() != null) {
			candidate = candidate.getParent();
		}
		return candidate;
	}

	protected void preventNonZeroExitCode() {
		System.exit(0);
	}

	static void setup(Thread thread) {
		UncaughtExceptionHandler handler = thread.getUncaughtExceptionHandler();
		if (!(handler instanceof SilentExitExceptionHandler)) {
			handler = new SilentExitExceptionHandler(handler);
			thread.setUncaughtExceptionHandler(handler);
		}
	}
	
	static void exitCurrentThread() {
		throw new SilentExitException();
	}
	
	
	// -------------------------------------------------------------------------------------
	// 内部类
	// -------------------------------------------------------------------------------------
	private static class SilentExitException extends RuntimeException {
		/** */
		private static final long serialVersionUID = -4584676323199201369L;
	}
}

package io.openk9.index.writer.internal.util;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReindexSemaphore {

	private ReindexSemaphore() {
		_semaphore = new AtomicBoolean(false);
	}

	public boolean tryLock() {
		return _semaphore.compareAndSet(false, true);
	}

	public void release() {
		_semaphore.compareAndSet(true, false);
	}

	public boolean hasReindexInProcess() {
		return _semaphore.get();
	}

	private final AtomicBoolean _semaphore;

	public static ReindexSemaphore getInstance() {
		return INSTANCE;
	}

	public static final ReindexSemaphore INSTANCE = new ReindexSemaphore();

}

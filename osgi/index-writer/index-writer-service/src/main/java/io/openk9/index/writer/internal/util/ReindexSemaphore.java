package io.openk9.index.writer.internal.util;

import java.util.concurrent.Semaphore;

public class ReindexSemaphore {

	private  ReindexSemaphore() {
		this(1);
	}

	private ReindexSemaphore(int slotLimit) {
		_semaphore = new Semaphore(slotLimit);
	}

	public boolean tryReindex() {
		return _semaphore.tryAcquire();
	}

	public void release() {
		_semaphore.release();
	}

	public boolean hasReindexInProcess() {
		return _semaphore.availablePermits() == 0;
	}

	private final Semaphore _semaphore;

	public static ReindexSemaphore getInstance() {
		return INSTANCE;
	}

	public static final ReindexSemaphore INSTANCE = new ReindexSemaphore();

}

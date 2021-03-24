package io.openk9.core.api.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class Async {

	public static void run(Runnable runnable) {
		run(runnable, false);
	}

	public static void await(Runnable runnable) {
		run(runnable, true);
	}

	public static void run(Runnable runnable, boolean await) {

		Future<Void> future =
			CompletableFuture.runAsync(runnable, COMMON_EXECUTOR);

		if (await) {
			try {
				future.get();
			}
			catch (Exception e) {
				if (_log.isErrorEnabled()) {
					_log.error(e.getMessage(), e);
				}
			}
		}

	}

	public static <T> CompletableFuture<T> future(Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier, COMMON_EXECUTOR);
	}

	public static <T> T await(Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier, COMMON_EXECUTOR).join();
	}

	private static final String COMMON_NAME = "OPENK9_SINGLE_THREAD";

	private static final ExecutorService COMMON_EXECUTOR =
		Executors.newSingleThreadExecutor(r -> new Thread(r, COMMON_NAME));

	private static final Logger _log = LoggerFactory.getLogger(
		Async.class.getName());

}

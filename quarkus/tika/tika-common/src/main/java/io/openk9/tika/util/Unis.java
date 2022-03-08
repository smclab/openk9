package io.openk9.tika.util;

import io.quarkus.runtime.BlockingOperationControl;
import io.quarkus.runtime.ExecutorRecorder;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.subscription.UniEmitter;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Unis {

	public static <T> Uni<T> toBlockingToUni(Consumer<UniEmitter<? super T>> emitterConsumer) {
		return Uni
			.createFrom()
			.emitter(emitterConsumer)
			.runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
	}

	public static <T> Uni<T> toBlockingToUni(UnSafeSupplier<T> supplier) {
		return runBlocking(supplier);
	}

	public static <T> Uni<T> runBlocking(Supplier<T> function) {
		return Uni.createFrom().deferred(() -> {
			if (BlockingOperationControl.isBlockingAllowed()) {
				try {
					return Uni.createFrom().item(function.get());
				} catch (Throwable t) {
					return Uni.createFrom().failure(t);
				}
			} else {
				return Uni.createFrom().emitter(
					uniEmitter -> ExecutorRecorder.getCurrent().execute(() -> {
						try {
							uniEmitter.complete(function.get());
						} catch (Throwable t) {
							uniEmitter.fail(t);
						}
					}));
			}
		});
	}

	interface UnSafeSupplier<T> extends Supplier<T> {
		T getThrow() throws Throwable;

		default T get() {
			try {
				return getThrow();
			}
			catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

}

/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

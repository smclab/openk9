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

package io.openk9.quarkus.common;

import java.util.function.Consumer;
import java.util.function.Supplier;
import jakarta.enterprise.inject.spi.CDI;

import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle;
import io.smallrye.common.vertx.VertxContext;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

public class VertxUtil {

	public static void runOnContext(Supplier<Uni<?>> supplier) {
		runOnContext(() -> supplier.get().subscribe().with(__ -> {}));
	}

	public static <T> void runOnContext(Supplier<Uni<T>> supplier, Consumer<T> consumer) {
		runOnContext(() -> supplier.get().subscribe().with(consumer));
	}
	public static void runOnContext(Runnable runnable) {
		runOnContext(CDI.current().select(Vertx.class).get(), runnable);
	}

	public static void runOnContext(Vertx vertx, Runnable runnable) {
		Context vertxContext = VertxContext.getOrCreateDuplicatedContext(vertx);
		VertxContextSafetyToggle.setContextSafe(vertxContext, true);
		vertxContext.runOnContext((ignore) -> runnable.run());
	}

}

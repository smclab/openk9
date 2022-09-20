package io.openk9.datasource.util;

import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle;
import io.smallrye.common.vertx.VertxContext;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

import java.util.function.Supplier;

public class VertxUtil {

	public static <T> Uni<T> contextRun(Vertx vertx, Supplier<Uni<T>> supplier) {
		return Uni
			.createFrom()
			.emitter((sink) -> {
				Context vertxContext =
					VertxContext.getOrCreateDuplicatedContext(vertx);
				VertxContextSafetyToggle.setContextSafe(vertxContext, true);
				vertxContext.runOnContext(
					unused -> supplier.get().subscribe().with(sink::complete, sink::fail));
			});
	}

}

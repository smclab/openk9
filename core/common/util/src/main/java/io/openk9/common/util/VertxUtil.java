package io.openk9.common.util;

import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle;
import io.smallrye.common.vertx.VertxContext;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

import javax.enterprise.inject.spi.CDI;
import java.util.function.Supplier;

public class VertxUtil {

	public static void runOnContext(Supplier<Uni<?>> supplier) {
		runOnContext(() -> supplier.get().subscribe().with(__ -> {}));
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

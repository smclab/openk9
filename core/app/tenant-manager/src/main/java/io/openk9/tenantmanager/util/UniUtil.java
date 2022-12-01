package io.openk9.tenantmanager.util;

import io.smallrye.mutiny.Uni;

import java.util.function.Supplier;

public class UniUtil {

	public static Uni<Void> fromSupplier(Supplier<?> supplier) {
		return Uni.createFrom().item(supplier).replaceWithVoid();
	}

	public static Uni<Void> fromRunnable(Runnable runnable) {
		return Uni.createFrom().item(() -> {
			runnable.run();
			return null;
		}).replaceWithVoid();
	}

}

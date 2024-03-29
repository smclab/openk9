package io.openk9.datasource.util;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.cache.CompositeCacheKey;
import io.smallrye.mutiny.Uni;

import java.util.concurrent.CompletableFuture;

public class QuarkusCacheUtil {

	public static <T> Uni<T> getAsync(
		Cache cache, CompositeCacheKey cacheKey, Uni<T> asyncValueLoader) {

		CompletableFuture<T> cachedValue = cache
			.as(CaffeineCache.class)
			.getIfPresent(cacheKey);

		if (cachedValue != null) {
			return Uni.createFrom().completionStage(cachedValue);
		}
		else {
			CompletableFuture<T> computedValue = asyncValueLoader.subscribe().asCompletionStage();
			cache.as(CaffeineCache.class).put(cacheKey, computedValue);
			return Uni.createFrom().completionStage(computedValue);
		}
	}

	public static void invalidateAllAsync(Cache cache) {
		cache
			.invalidateAll()
			.subscribe()
			.with(unused -> {});
	}

	public final QuarkusCacheUtil INSTANCE = new QuarkusCacheUtil();
	private QuarkusCacheUtil() {}
}

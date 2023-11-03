package io.openk9.datasource.cache;

import io.openk9.datasource.actor.ActorSystemBehaviorInitializer;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Set;

public class CacheHandlerInitializer {

	@Produces
	@ApplicationScoped
	ActorSystemBehaviorInitializer cacheHandlerBehaviorInit() {
		return ctx -> ctx.spawnAnonymous(
			P2PCache.create(Set.of(bucketResourceCache, searcherServiceCache))
		);
	}


	@CacheName("bucket-resource")
	Cache bucketResourceCache;

	@CacheName("searcher-service")
	Cache searcherServiceCache;
}

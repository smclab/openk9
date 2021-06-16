package io.openk9.entity.manager.api;

import io.openk9.entity.manager.model.Entity;
import org.neo4j.cypherdsl.core.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

public interface EntityGraphRepository {

	Mono<Entity> addEntity(
		long tenantId, String entityName, String entityType);

	Flux<Entity> addEntities(List<Entity> entities);

	Mono<Entity> getEntity(long id);

	Flux<Entity> getEntities(long tenantId, String entityType);

	Flux<Entity> getEntities(Statement statement);

}

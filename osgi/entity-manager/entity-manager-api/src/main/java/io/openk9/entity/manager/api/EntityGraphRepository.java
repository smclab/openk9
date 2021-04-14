package io.openk9.entity.manager.api;

import io.openk9.entity.manager.model.DocumentEntity;
import io.openk9.entity.manager.model.Entity;
import org.elasticsearch.action.search.SearchRequest;
import org.neo4j.cypherdsl.core.Statement;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EntityGraphRepository {

	Mono<Entity> addEntity(
		long tenantId, String entityName, String entityType);

	Mono<Entity> getEntity(long id);

	Flux<Entity> getEntities(long tenantId, String entityType);

	Flux<Entity> getEntities(Statement statement);

	Flux<DocumentEntity> getEntities(SearchRequest searchRequest);

}

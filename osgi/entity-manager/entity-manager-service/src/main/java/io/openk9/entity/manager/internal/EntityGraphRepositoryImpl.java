package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.EntityGraphRepository;
import io.openk9.entity.manager.model.Entity;
import io.openk9.relationship.graph.api.client.GraphClient;
import io.openk9.relationship.graph.api.client.Record;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Functions;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Property;
import org.neo4j.cypherdsl.core.Statement;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.neo4j.cypherdsl.core.Cypher.literalOf;

@Component(
	immediate = true,
	service = EntityGraphRepository.class
)
public class EntityGraphRepositoryImpl implements EntityGraphRepository {

	@Override
	public Mono<Entity> addEntity(
		long tenantId, String entityName, String entityType) {

		Node entity =
			Cypher
				.node(entityType)
				.named("entity")
				.withProperties(
					"entityName", literalOf(entityName),
					"tenantId", literalOf(tenantId)
				);

		Statement statement =
			Cypher
				.create(entity)
				.returning(entity)
				.build();

		return Mono
			.from(_graphClient.write(statement))
			.transform(this::_recordToEntity);

	}

	@Override
	public Mono<Entity> getEntity(long id) {

		Node node = Cypher.anyNode("e");

		Statement statement = Cypher
			.match(node)
			.where(
				Functions.id(node).eq(literalOf(id))
			)
			.returning(node)
			.build();

		return Mono
			.from(_graphClient.read(statement))
			.transform(this::_recordToEntity);

	}

	@Override
	public Flux<Entity> getEntities(long tenantId, String entityType) {

		Node entity =
			Cypher
				.node(entityType)
				.named("entity");

		Property tenantIdProperty = entity.property("tenantId");

		Statement statement =
			Cypher
				.match(entity)
				.where(tenantIdProperty.eq(literalOf(tenantId)))
				.returning(entity)
				.build();

		return Flux.from(getEntities(statement));
	}

	@Override
	public Publisher<Entity> getEntities(Statement statement) {
		return Flux
			.from(_graphClient.read(statement))
			.concatMap(recordMono -> _recordToEntity(Mono.just(recordMono)));
	}

	private String _getFirstEntry(Iterable<String> iterable) {
		return iterable.iterator().next();
	}

	private Mono<Entity> _recordToEntity(Mono<Record> mono) {
		return mono
			.map(record -> record.get(0).asNode())
			.map(node -> Entity
				.builder()
				.tenantId(node.get("tenantId").asLong())
				.entityName(node.get("entityName").asString())
				.id(node.id())
				.entityType(_getFirstEntry(node.labels()))
				.build()
			);
	}

	@Reference
	private GraphClient _graphClient;

}

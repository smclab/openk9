package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.Constants;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
				.named(Constants.ENTITY)
				.withProperties(
					Constants.ENTITY_NAME_FIELD, literalOf(entityName),
					Constants.ENTITY_TENANT_ID_FIELD, literalOf(tenantId)
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
				.named(Constants.ENTITY);

		Property tenantIdProperty = entity.property(Constants.ENTITY_TENANT_ID_FIELD);

		Statement statement =
			Cypher
				.match(entity)
				.where(tenantIdProperty.eq(literalOf(tenantId)))
				.returning(entity)
				.build();

		return Flux.from(getEntities(statement));
	}

	@Override
	public Flux<Entity> getEntities(Statement statement) {
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
				.tenantId(node.get(Constants.ENTITY_TENANT_ID_FIELD).asLong())
				.name(node.get(Constants.ENTITY_NAME_FIELD).asString())
				.id(node.id())
				.type(_getFirstEntry(node.labels()))
				.build()
			);
	}

	private long _objectToLong(Object obj) {
		if (obj instanceof String) {
			return Long.parseLong((String)obj);
		}
		else if (obj instanceof Number) {
			return ((Number)obj).longValue();
		}
		else {
			return -1;
		}
	}

	@Reference
	private GraphClient _graphClient;

	private static final Logger _log = LoggerFactory.getLogger(
		EntityGraphRepositoryImpl.class);

}

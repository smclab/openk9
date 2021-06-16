package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.Constants;
import io.openk9.entity.manager.api.EntityGraphRepository;
import io.openk9.entity.manager.model.Entity;
import io.openk9.relationship.graph.api.client.GraphClient;
import io.openk9.relationship.graph.api.client.Record;
import io.openk9.relationship.graph.api.client.Value;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Functions;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.PatternElement;
import org.neo4j.cypherdsl.core.Property;
import org.neo4j.cypherdsl.core.Statement;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

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
	public Flux<Entity> addEntities(List<Entity> entities) {

		PatternElement[] nodes =
			IntStream
				.range(0, entities.size())
				.mapToObj(i -> {

					Entity entity = entities.get(i);

					return Cypher
						.node(entity.getType())
						.named(RANDOM_NAME[i])
						.withProperties(
							Constants.ENTITY_NAME_FIELD, literalOf(entity.getName()),
							Constants.ENTITY_TENANT_ID_FIELD, literalOf(entity.getTenantId())
						);
					})
				.toArray(PatternElement[]::new);

		Statement statement =
			Cypher
				.create(nodes)
				.returning(Arrays.stream(nodes).map(patternElement -> ((Node)patternElement)).toArray(Node[]::new))
				.build();

		return Flux
			.from(_graphClient.write(statement))
			.concatMap(record -> _recordToToListEntity(Mono.just(record)));
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

	private Flux<Entity> _recordToToListEntity(Mono<Record> mono) {
		return mono
			.flatMapIterable(Record::values)
			.map(Value::asNode)
			.map(node -> Entity
				.builder()
				.tenantId(node.get(Constants.ENTITY_TENANT_ID_FIELD).asLong())
				.name(node.get(Constants.ENTITY_NAME_FIELD).asString())
				.id(node.id())
				.type(_getFirstEntry(node.labels()))
				.build()
			);
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

	private static final String[] RANDOM_NAME = {
		"Bulbasaur", "Ivysaur", "Venusaur", "Charmander", "Charmeleon",
		"Charizard", "Squirtle", "Wartortle", "Blastoise",
		"Caterpie", "Metapod", "Butterfree", "Weedle", "Kakuna", "Beedrill",
		"Pidgey", "Pidgeotto", "Pidgeot", "Rattata", "Raticate", "Spearow",
		"Fearow", "Ekans", "Arbok", "Pikachu", "Raichu", "Sandshrew",
		"Sandslash", "Nidorana", "Nidorina", "Nidoqueen", "Nidoran",
		"Nidorino", "Nidoking", "Clefairy", "Clefable", "Vulpix", "Ninetales",
		"Jigglypuff", "Wigglytuff", "Zubat", "Golbat", "Oddish", "Gloom",
		"Vileplume", "Paras", "Parasect", "Venonat", "Venomoth", "Diglett",
		"Dugtrio", "Meowth", "Persian", "Psyduck", "Golduck", "Mankey",
		"Primeape", "Growlithe", "Arcanine", "Poliwag", "Poliwhirl",
		"Poliwrath", "Abra", "Kadabra", "Alakazam", "Machop", "Machoke",
		"Machamp", "Bellsprout", "Weepinbell", "Victreebel", "Tentacool",
		"Tentacruel", "Geodude", "Graveler", "Golem", "Ponyta", "Rapidash",
		"Slowpoke", "Slowbro", "Magnemite", "Magneton", "Farfetch’d", "Doduo",
		"Dodrio", "Seel", "Dewgong", "Grimer", "Muk", "Shellder", "Cloyster",
		"Gastly", "Haunter", "Gengar", "Onix"
	};

}

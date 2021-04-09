package io.openk9.entity.manager.http;

import io.openk9.entity.manager.api.EntityGraphRepository;
import io.openk9.entity.manager.api.EntityNameCleanerProvider;
import io.openk9.entity.manager.model.Entity;
import io.openk9.entity.manager.model.payload.EntityRequest;
import io.openk9.entity.manager.model.payload.RelationRequest;
import io.openk9.entity.manager.model.payload.Request;
import io.openk9.entity.manager.model.payload.Response;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.json.api.JsonFactory;
import io.openk9.relationship.graph.api.client.GraphClient;
import org.neo4j.cypherdsl.core.AliasedExpression;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Functions;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.SymbolicName;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.neo4j.cypherdsl.core.Cypher.literalOf;

@Component(
	immediate = true,
	service = Endpoint.class
)
public class GetOrAddEntitiesHttpHandler implements HttpHandler {

	@Override
	public int method() {
		return POST;
	}

	@Override
	public String getPath() {
		return "/entity-manager/get-or-add-entities";
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		Mono<Request> request = _validateRequest(httpRequest);

		Flux<Response> entityFlux = request.flatMapMany(this::_getOrAddEntities);

		return _httpResponseWriter.write(httpResponse, entityFlux);
	}

	private Flux<Response> _getOrAddEntities(Request request) {

		long tenantId = request.getTenantId();

		List<EntityRequest> entityRequests = request.getEntities();

		Mono<Map<EntityRequest, List<Entity>>> entityMap =
			Flux
				.fromIterable(entityRequests)
				.concatMap(er -> {

						Statement statement = _entityNameCleanerProvider
							.getEntityNameCleaner(er.getEntityType())
							.cleanEntityName(tenantId, er.getEntityName());

						return Mono.zip(
							Mono.just(er),
							Flux.from(_entityGraphRepository.getEntities(statement)).collectList(),
							Map::entry);
					}
				)
				.collectMap(Map.Entry::getKey, Map.Entry::getValue);


		return entityMap.flatMapMany(map -> {

			List<Mono<Tuple2<EntityRequest, Entity>>> result = new ArrayList<>();

			for (Map.Entry<EntityRequest, List<Entity>> entry : map.entrySet()) {

				EntityRequest currentEntityRequest = entry.getKey();

				List<Entity> entityRequestListWithoutCurrentEntityRequest =
					map
						.entrySet()
						.stream()
						.filter(entityRequestListEntry ->
							entityRequestListEntry.getKey() != currentEntityRequest)
						.map(Map.Entry::getValue)
						.flatMap(Collection::stream)
						.collect(Collectors.toList());

				result.add(_disambiguate(
					entry.getValue(), entityRequestListWithoutCurrentEntityRequest,
					tenantId, currentEntityRequest)
				);

			}

			return Flux.concat(result);

		})
			.collectMap(Tuple2::getT1, Tuple2::getT2)
			.flatMapMany(map -> {

				List<Statement> statementList = new ArrayList<>();

				for (Map.Entry<EntityRequest, Entity> entrySet : map.entrySet()) {

					List<RelationRequest> relations =
						entrySet.getKey().getRelations();

					if (relations == null || relations.isEmpty()) {
						continue;
					}

					Entity currentEntity = entrySet.getValue();

					List<Tuple2<String, Entity>> entityRelations =
						map
							.entrySet()
							.stream()
							.flatMap(entry -> {

								for (RelationRequest relation : relations) {
									if (entry.getKey().getTmpId() == relation.getTo()) {
										return Stream.of(
											Tuples.of(
												relation.getName(),
												entry.getValue())
										);
									}
								}

								return Stream.empty();

							})
							.collect(Collectors.toList());

					Node currentEntityNode =
						Cypher
							.node(currentEntity.getEntityType())
							.named("a");

					List<Statement> currentStatementList =
						entityRelations
							.stream()
							.map(t2 -> {

								Entity entityRelation = t2.getT2();

								Node entityRelationNode =
									Cypher
										.node(entityRelation.getEntityType())
										.named("b");

								return Cypher
									.match(currentEntityNode, entityRelationNode)
									.where(
										Functions
											.id(currentEntityNode)
											.eq(literalOf(currentEntity.getId()))
											.and(
												Functions
													.id(entityRelationNode)
													.eq(literalOf(
														entityRelation.getId()))
											)
									)
									.merge(
										currentEntityNode
											.relationshipTo(
												entityRelationNode, t2.getT1())
									)
									.build();
							})
							.collect(Collectors.toList());

					statementList.addAll(currentStatementList);

				}

				List<Response> response =
					map
						.entrySet()
						.stream()
						.map(t2 -> Response
							.builder()
							.entity(t2.getValue())
							.tmpId(t2.getKey().getTmpId())
							.build()
						)
						.collect(Collectors.toList());

				if (statementList.size() > 1) {
					return Flux.from(_graphClient.write(
						Cypher.unionAll(statementList.toArray(new Statement[0]))
					))
						.thenMany(Flux.fromIterable(response));
				}
				else if (statementList.size() == 1) {
					return Flux.from(_graphClient.write(statementList.get(0)))
						.thenMany(Flux.fromIterable(response));
				}
				else {
					return Flux.fromIterable(response);
				}

			});
	}

	private Mono<Tuple2<EntityRequest, Entity>> _disambiguate(
		List<Entity> candidates, List<Entity> entityRequestList,
		long tenantId, EntityRequest currentEntityRequest) {

		Statement[] statements = new Statement[entityRequestList.size()];

		for (int i = 0; i < entityRequestList.size(); i++) {

			Entity entityRequest = entityRequestList.get(i);

			Node nodeEntity =
				Cypher.node(entityRequest.getEntityType()).named("entity");

			AliasedExpression entityAliased = nodeEntity.as("entity");

			SymbolicName path = Cypher.name("path");

			Statement statement = Cypher
				.match(nodeEntity)
				.where(Functions.id(nodeEntity).eq(
					literalOf(entityRequest.getId())))
				.call("apoc.path.expand").withArgs(
					entityAliased.getDelegate(), literalOf(null),
					literalOf(null), literalOf(1), literalOf(2))
				.yield(path)
				.returning(
					Functions.last(Functions.nodes(path)).as("node"),
					Functions.size(Functions.nodes(path)).subtract(
						literalOf(1)).as("hops"))
				.build();

			statements[i] = statement;

		}

		Flux<Entity> entityFlux = Flux.empty();

		if (statements.length == 1) {
			Statement entityRequestListStatement =
				Cypher
					.call(statements[0])
					.returning("node", "hops")
					.orderBy(Cypher.name("hops"))
					.build();

			entityFlux = Flux.from(_entityGraphRepository.getEntities(entityRequestListStatement));

		}
		else if (statements.length > 1) {
			Statement entityRequestListStatement =
				Cypher
					.call(Cypher.unionAll(statements))
					.returning("node", "hops")
					.orderBy(Cypher.name("hops"))
					.build();

			entityFlux = Flux.from(_entityGraphRepository.getEntities(entityRequestListStatement));
		}

		return entityFlux
			.filter(entity -> candidates
				.stream()
				.anyMatch(entity1 -> entity1.getId() == entity.getId()))
			.next()
			.switchIfEmpty(Mono.defer(() -> _entityGraphRepository.addEntity(
				tenantId, currentEntityRequest.getEntityName(),
				currentEntityRequest.getEntityType())))
			.map(entity -> Tuples.of(currentEntityRequest, entity));

	}


	private Mono<Request> _validateRequest(HttpRequest httpRequest) {
		return Mono
			.from(httpRequest.aggregateBodyToByteArray())
			.map(bytes -> _jsonFactory.fromJson(bytes, Request.class));
	}


	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private HttpResponseWriter _httpResponseWriter;

	@Reference
	private EntityGraphRepository _entityGraphRepository;

	@Reference
	private EntityNameCleanerProvider _entityNameCleanerProvider;

	@Reference
	private GraphClient _graphClient;

	private static final Logger _log = LoggerFactory.getLogger(
		GetOrAddEntitiesHttpHandler.class);

}

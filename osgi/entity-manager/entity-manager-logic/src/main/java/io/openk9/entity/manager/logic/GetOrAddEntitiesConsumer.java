package io.openk9.entity.manager.logic;


import io.openk9.entity.manager.model.Entity;
import io.openk9.entity.manager.model.payload.EntityRequest;
import io.openk9.entity.manager.model.payload.RelationRequest;
import io.openk9.entity.manager.model.payload.Request;
import io.openk9.entity.manager.model.payload.Response;
import io.openk9.entity.manager.model.payload.ResponseList;
import io.openk9.entity.manager.subscriber.api.EntityManagerRequestConsumer;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.BindingRegistry;
import io.openk9.ingestion.api.BundleSender;
import io.openk9.ingestion.api.BundleSenderProvider;
import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;
import io.openk9.relationship.graph.api.client.GraphClient;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Functions;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Statement;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.neo4j.cypherdsl.core.Cypher.literalOf;

@Component(
	immediate = true,
	service = GetOrAddEntitiesConsumer.class
)
public class GetOrAddEntitiesConsumer  {

	@interface Config {
		boolean transactional() default false;
		long timeout() default 30;
		int prefetch() default 1;
		int concurrency() default 8;
	}

	@Activate
	void activate(Config config) {
		_transactional = config.transactional();

		_disposable = _entityManagerRequestConsumer
			.stream(config.prefetch())
			.flatMap(this::apply, config.concurrency())
			.flatMap(objectNode -> {

				String replyTo = objectNode.get("replyTo").asText();

				_bindingRegistry.register(
					Binding.Exchange.of(
						"amq.topic", Binding.Exchange.Type.topic),
						replyTo
					);

				BundleSender bundleSender =
					_bundleSenderProvider.getBundleSender(replyTo);

				return bundleSender.send(
					Mono.just(objectNode.toString().getBytes())
				);

			})
			.subscribe();

	}

	@Modified
	void modified(Config config) {
		deactivate();
		activate(config);
	}

	@Deactivate
	void deactivate() {
		_disposable.dispose();
	}


	public Mono<ObjectNode> apply(ObjectNode objectNode) {

		String replyTo = objectNode.get("replyTo").asText();

		ObjectNode datasourceContextJson =
			objectNode.get("datasourceContext").toObjectNode();

		long datasourceId = datasourceContextJson
			.get("datasource")
			.get("datasourceId")
			.asLong();

		long tenantId = datasourceContextJson
			.get("tenant")
			.get("tenantId")
			.asLong();

		JsonNode entities = objectNode.remove("entities");

		ObjectNode responseJson = _jsonFactory.createObjectNode();

		responseJson.put("entities", entities);
		responseJson.put("tenantId", tenantId);
		responseJson.put("datasourceId", datasourceId);

		Request request =
			_jsonFactory.fromJson(responseJson.toString(), Request.class);

		List<RequestContext> requestContextList =
			request
				.getEntities()
				.stream()
				.map(entityRequest -> RequestContext
					.builder()
					.current(entityRequest)
					.tenantId(request.getTenantId())
					.datasourceId(request.getDatasourceId())
					.rest(
						request
							.getEntities()
							.stream()
							.filter(er -> er != entityRequest)
							.collect(Collectors.toList())
					)
					.build()
				)
				.collect(Collectors.toList());

		Mono<List<EntityContext>> disambiguateListMono =
			GetOrAddEntities.stopWatch(
				"disambiguate-all-entities",
				Flux.fromIterable(requestContextList)
					.flatMap(
						requestContext ->
							GetOrAddEntities.stopWatch(
								"disambiguate-" + requestContext.getCurrent().getName(),
								Mono.<EntityContext>create(
									fluxSink ->
										_startDisambiguation.disambiguate(
											requestContext, fluxSink)
								)
							)
					)
					.collectList()
			);

		Mono<ResponseList> writeRelations = disambiguateListMono
			.flatMap(entityContexts ->
				GetOrAddEntities.stopWatch(
					"write-relations", writeRelations(entityContexts)));

		Mono<ResponseList> responseListWrapper =
			_transactional
				? _graphClient.makeTransactional(writeRelations)
				: writeRelations;

		Mono<ArrayNode> entitiesField = responseListWrapper
			.map(responseListDTO -> {

				List<Response> responseList = responseListDTO.getResponse();

				ArrayNode entitiesArrayNode = entities.toArrayNode();

				ArrayNode arrayNode = _jsonFactory.createArrayNode();

				for (JsonNode node : entitiesArrayNode) {

					Optional<Response> responseOptional =
						responseList
							.stream()
							.filter(response ->
								node.get("tmpId").asLong() ==
								response.getTmpId())
							.findFirst();

					if (responseOptional.isPresent()) {

						Entity entity = responseOptional.get().getEntity();

						ObjectNode result = _jsonFactory.createObjectNode();

						result.put("entityType", entity.getType());

						result.put("id", entity.getId());

						result.put("context", node.get("context"));

						arrayNode.add(result);

					}

				}

				return arrayNode;

			});

		return entitiesField.map(
			entitiesArray -> {

				ObjectNode payload = objectNode.get("payload").toObjectNode();

				payload.set("entities", entitiesArray);

				return objectNode;

			});

	}

	public Mono<ResponseList> writeRelations(List<EntityContext> entityContext) {

		return Mono.defer(() -> {

			List<Statement> statementList = new ArrayList<>();

			for (EntityContext context : entityContext) {

				EntityRequest entityRequest = context.getEntityRequest();

				List<RelationRequest> relations =
					entityRequest.getRelations();

				if (relations == null || relations.isEmpty()) {
					continue;
				}

				Entity currentEntity = context.getEntity();

				List<Tuple2<String, Entity>> entityRelations =
					entityContext
						.stream()
						.flatMap(entry -> {

							for (RelationRequest relation : relations) {
								if (entry.getEntityRequest().getTmpId() == relation.getTo()) {
									return Stream.of(
										Tuples.of(
											relation.getName(),
											entry.getEntity())
									);
								}
							}

							return Stream.empty();

						})
						.collect(Collectors.toList());

				Node currentEntityNode =
					Cypher
						.node(currentEntity.getType())
						.named("a");

				List<Statement> currentStatementList =
					entityRelations
						.stream()
						.map(t2 -> {

							Entity entityRelation = t2.getT2();

							Node entityRelationNode =
								Cypher
									.node(entityRelation.getType())
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
				entityContext
					.stream()
					.map(context -> Response
						.builder()
						.entity(
							Entity
								.builder()
								.name(context.getEntity().getName())
								.id(context.getEntity().getId())
								.tenantId(context.getEntity().getTenantId())
								.type(context.getEntity().getType())
								.build()
						)
						.tmpId(context.getEntityRequest().getTmpId())
						.build()
					)
					.collect(Collectors.toList());

			if (statementList.size() > 1) {
				return _graphClient
					.write(Cypher.unionAll(statementList.toArray(new Statement[0])))
					.then(Mono.just(ResponseList.of("", response)));
			}
			else if (statementList.size() == 1) {
				return _graphClient
					.write(statementList.get(0))
					.then(Mono.just(ResponseList.of("", response)));
			}
			else {
				return Mono.just(ResponseList.of("", response));
			}

		});

	}

	private boolean _transactional;

	private Disposable _disposable;

	@Reference
	private GraphClient _graphClient;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private StartDisambiguation _startDisambiguation;

	@Reference
	private EntityManagerRequestConsumer _entityManagerRequestConsumer;

	@Reference
	private BindingRegistry _bindingRegistry;

	@Reference
	private BundleSenderProvider _bundleSenderProvider;

	private static final Logger _log = LoggerFactory.getLogger(
		GetOrAddEntitiesConsumer.class);

}

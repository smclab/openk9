package io.openk9.entity.manager.http;

import io.openk9.entity.manager.api.EntityGraphRepository;
import io.openk9.entity.manager.api.EntityNameCleanerProvider;
import io.openk9.entity.manager.model.DocumentEntity;
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
import org.elasticsearch.action.search.SearchRequest;
import org.neo4j.cypherdsl.core.AliasedExpression;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Functions;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.SymbolicName;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.Arrays;
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

	@interface Config {
		double scoreThreshold() default 0.9;
	    int minHops() default 1;
		int maxHops() default 2;
	}

	@Activate
	void activate(Config config) {
		_maxHops = config.maxHops();
		_minHops = config.minHops();
		_scoreThreshold = config.scoreThreshold();
	}

	@Modified
	void modified(Config config) {
		activate(config);
	}

	private Flux<Response> _getOrAddEntities(Request request) {

		long tenantId = request.getTenantId();

		List<EntityRequest> entityRequests = request.getEntities();

		Mono<Map<EntityRequest, List<DocumentEntity>>> entityMap =
			Flux
				.fromIterable(entityRequests)
				.concatMap(er -> {

					SearchRequest searchRequest = _entityNameCleanerProvider
						.getEntityNameCleaner(er.getType())
						.cleanEntityName(tenantId, er.getName());

					return Mono.zip(
							Mono.just(er),
							_entityGraphRepository
								.getEntities(searchRequest)
								.collectList(),
							Map::entry);
					}
				)
				.collectMap(Map.Entry::getKey, Map.Entry::getValue);


		return entityMap.flatMapMany(map -> {

			List<Mono<Tuple2<EntityRequest, DocumentEntity>>> result = new ArrayList<>();

			for (Map.Entry<EntityRequest, List<DocumentEntity>> entry : map.entrySet()) {

				EntityRequest currentEntityRequest = entry.getKey();

				List<DocumentEntity> entityRequestListWithoutCurrentEntityRequest =
					map
						.entrySet()
						.stream()
						.filter(entityRequestListEntry ->
							entityRequestListEntry.getKey() != currentEntityRequest)
						.map(Map.Entry::getValue)
						.flatMap(Collection::stream)
						.collect(Collectors.toList());

				Mono<Tuple2<EntityRequest, DocumentEntity>>
					entityRequestDocumentEntityMono;

				List<DocumentEntity> candidates = entry.getValue();

				if (_checkDisambiguate(currentEntityRequest, candidates)) {

					if (_log.isInfoEnabled()) {
						_log.info("doing disambiguation");
					}

					entityRequestDocumentEntityMono =
						_disambiguate(
							candidates,
							entityRequestListWithoutCurrentEntityRequest,
							tenantId, currentEntityRequest);
				}
				else {

					DocumentEntity documentEntity = candidates.get(0);

					if (_log.isInfoEnabled()) {
						_log.info(
							"found candidate with " +
							"id: " + documentEntity.getId() + " " +
							"score: " + documentEntity.getScore());
					}

					entityRequestDocumentEntityMono =
						Mono.just(
							Tuples.of(
								currentEntityRequest,
								documentEntity
							)
						);
				}

				result.add(entityRequestDocumentEntityMono);

			}

			return Flux.concat(result);

		})
			.collectMap(Tuple2::getT1, Tuple2::getT2)
			.flatMapMany(map -> {

				List<Statement> statementList = new ArrayList<>();

				for (Map.Entry<EntityRequest, DocumentEntity> entrySet : map.entrySet()) {

					List<RelationRequest> relations =
						entrySet.getKey().getRelations();

					if (relations == null || relations.isEmpty()) {
						continue;
					}

					DocumentEntity currentEntity = entrySet.getValue();

					List<Tuple2<String, DocumentEntity>> entityRelations =
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
							.node(currentEntity.getType())
							.named("a");

					List<Statement> currentStatementList =
						entityRelations
							.stream()
							.map(t2 -> {

								DocumentEntity entityRelation = t2.getT2();

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
					map
						.entrySet()
						.stream()
						.map(t2 -> Response
							.builder()
							.entity(
								Entity
									.builder()
									.name(t2.getValue().getName())
									.id(t2.getValue().getId())
									.tenantId(t2.getValue().getTenantId())
									.type(t2.getValue().getType())
									.build()
							)
							.tmpId(t2.getKey().getTmpId())
							.build()
						)
						.collect(Collectors.toList());

				if (statementList.size() > 1) {
					return _graphClient
						.write(Cypher.unionAll(statementList.toArray(new Statement[0])))
						.thenMany(Flux.fromIterable(response));
				}
				else if (statementList.size() == 1) {
					return _graphClient
						.write(statementList.get(0))
						.thenMany(Flux.fromIterable(response));
				}
				else {
					return Flux.fromIterable(response);
				}

			});
	}

	private boolean _checkDisambiguate(
		EntityRequest entityRequest,
		List<DocumentEntity> candidates) {

		if (!candidates.isEmpty()) {

			DocumentEntity documentEntity = candidates.get(0);

			double bestScore;

			if (candidates.size() > 1) {

				if (_log.isDebugEnabled()) {
					_log.debug("softmax");
				}

				double[] scores = candidates
					.stream()
					.mapToDouble(DocumentEntity::getScore)
					.toArray();

				bestScore = _softmax(documentEntity.getScore(), scores);

			}
			else {

				if (_log.isDebugEnabled()) {
					_log.debug("levenshtein");
				}

				bestScore = _levenshteinDistance(
					documentEntity.getName(),
					_entityNameCleanerProvider
						.getEntityNameCleaner(entityRequest.getType())
						.cleanEntityName(entityRequest.getName())
				);

			}

			if (_log.isDebugEnabled()) {
				_log.debug(
					"current score: " + bestScore + " score threshold: " + _scoreThreshold);
			}

			return bestScore < _scoreThreshold;

		}

		return true;
	}

	private static double _levenshteinDistance(String x, String y) {

		int xLength = x.length();
		int yLength = y.length();

		int[][] dp = new int[xLength + 1][yLength + 1];

		for (int i = 0; i <= xLength; i++) {
			for (int j = 0; j <= yLength; j++) {
				if (i == 0) {
					dp[i][j] = j;
				}
				else if (j == 0) {
					dp[i][j] = i;
				}
				else {
					dp[i][j] = _min(dp[i - 1][j - 1]
									+ _costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
						dp[i - 1][j] + 1,
						dp[i][j - 1] + 1);
				}
			}
		}

		return 1 - ((double)dp[xLength][yLength] / Math.max(xLength, yLength));
	}

	public static int _min(int... numbers) {
		return Arrays.stream(numbers)
			.min().orElse(Integer.MAX_VALUE);
	}

	public static int _costOfSubstitution(char a, char b) {
		return a == b ? 0 : 1;
	}

	private static double _softmax(double input, double[] neuronValues) {

		double total = Arrays
			.stream(neuronValues)
			.map(Math::exp)
			.sum();

		return Math.exp(input) / total;
	}

	private Mono<Tuple2<EntityRequest, DocumentEntity>> _disambiguate(
		List<DocumentEntity> candidates, List<DocumentEntity> entityRequestList,
		long tenantId, EntityRequest currentEntityRequest) {

		Flux<Entity> entityFlux = Flux.empty();

		if (!candidates.isEmpty()) {

			Statement[] statements = new Statement[entityRequestList.size()];

			for (int i = 0; i < entityRequestList.size(); i++) {

				DocumentEntity entityRequest = entityRequestList.get(i);

				Node nodeEntity =
					Cypher.node(entityRequest.getType()).named(ENTITY);

				AliasedExpression entityAliased = nodeEntity.as(ENTITY);

				SymbolicName path = Cypher.name(PATH);

				Statement statement = Cypher
					.match(nodeEntity)
					.where(Functions.id(nodeEntity).eq(
						literalOf(entityRequest.getId())))
					.call(APOC_PATH_EXPAND).withArgs(
						entityAliased.getDelegate(), literalOf(null),
						literalOf(null), literalOf(_minHops), literalOf(_maxHops))
					.yield(path)
					.returning(
						Functions.last(Functions.nodes(path)).as(NODE),
						Functions.size(Functions.nodes(path)).subtract(
							literalOf(1)).as(HOPS))
					.build();

				statements[i] = statement;

			}

			if (statements.length == 1) {
				Statement entityRequestListStatement =
					Cypher
						.call(statements[0])
						.returning(NODE, HOPS)
						.orderBy(Cypher.name(HOPS))
						.build();

				entityFlux = _entityGraphRepository.getEntities(
					entityRequestListStatement);

			}
			else if (statements.length > 1) {
				Statement entityRequestListStatement =
					Cypher
						.call(Cypher.unionAll(statements))
						.returning(NODE, HOPS)
						.orderBy(Cypher.name(HOPS))
						.build();

				entityFlux = _entityGraphRepository.getEntities(
					entityRequestListStatement);
			}

		}

		return entityFlux
			.filter(entity -> candidates
				.stream()
				.anyMatch(entity1 -> entity1.getId() == entity.getId()))
			.next()
			.switchIfEmpty(Mono.defer(() -> _entityGraphRepository.addEntity(
				tenantId, currentEntityRequest.getName(),
				currentEntityRequest.getType())))
			.map(entity -> Tuples.of(
				currentEntityRequest,
				DocumentEntity
					.builder()
					.tenantId(entity.getTenantId())
					.type(entity.getType())
					.name(entity.getName())
					.id(entity.getId())
					.score(1L)
					.build()
				)
			);

	}


	private Mono<Request> _validateRequest(HttpRequest httpRequest) {
		return Mono
			.from(httpRequest.aggregateBodyToByteArray())
			.map(bytes -> _jsonFactory.fromJson(bytes, Request.class));
	}

	private double _scoreThreshold;

	private int _minHops;

	private int _maxHops;

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

	private static final String ENTITY = "entity";
	private static final String PATH = "path";
	private static final String APOC_PATH_EXPAND = "apoc.path.expand";
	private static final String NODE = "node";
	private static final String HOPS = "hops";

	private static final Logger _log = LoggerFactory.getLogger(
		GetOrAddEntitiesHttpHandler.class);

}

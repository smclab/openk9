package io.openk9.entity.manager.logic;


import io.openk9.entity.manager.api.EntityGraphRepository;
import io.openk9.entity.manager.api.EntityNameCleanerProvider;
import io.openk9.entity.manager.model.Entity;
import io.openk9.entity.manager.model.payload.EntityRequest;
import io.openk9.entity.manager.model.payload.RelationRequest;
import io.openk9.entity.manager.model.payload.Request;
import io.openk9.entity.manager.model.payload.Response;
import io.openk9.entity.manager.model.payload.ResponseList;
import io.openk9.entity.manager.pub.sub.api.MessageRequest;
import io.openk9.entity.manager.pub.sub.api.MessageResponse;
import io.openk9.entity.manager.publisher.api.EntityManagerResponsePublisher;
import io.openk9.entity.manager.subscriber.api.EntityManagerRequestConsumer;
import io.openk9.index.writer.entity.client.api.IndexWriterEntityClient;
import io.openk9.index.writer.entity.model.DocumentEntityRequest;
import io.openk9.index.writer.entity.model.DocumentEntityResponse;
import io.openk9.relationship.graph.api.client.GraphClient;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.neo4j.cypherdsl.core.Cypher.literalOf;

@Component(
	immediate = true,
	service = GetOrAddEntitiesConsumer.class
)
public class GetOrAddEntitiesConsumer {

	@interface Config {
		double scoreThreshold() default 0.9;
		int minHops() default 1;
		int maxHops() default 2;
		String[] uniqueEntities() default {"date", "organization"};
		String labelFilter() default "-date";
	}

	@Activate
	void activate(Config config) {

		_maxHops = config.maxHops();
		_minHops = config.minHops();
		_scoreThreshold = config.scoreThreshold();
		_uniqueEntities = config.uniqueEntities();
		_labelFilter = config.labelFilter();

		_entityManagerRequestConsumer
			.stream(1)
			.flatMap(this::_handleMessage)
			.doOnNext(list -> _entityManagerResponsePublisher.publish(
				MessageResponse
					.builder()
					.response(list)
					.build()
			))
			.subscribe();
	}

	@Modified
	void modified(Config config) {
		activate(config);
	}

	private Mono<ResponseList> _handleMessage(MessageRequest messageRequest) {

		Request request = messageRequest.getRequest();

		long tenantId = request.getTenantId();

		List<EntityRequest> entityRequests = request.getEntities();

		Mono<Map<EntityRequest, List<DocumentEntityResponse>>> entityMap =
			Flux
				.fromIterable(entityRequests)
				.concatMap(er -> {

						Map<String, Object> stringObjectMap =
							_entityNameCleanerProvider
								.getEntityNameCleaner(er.getType())
								.cleanEntityName(tenantId, er.getName());

						return Mono.zip(
							Mono.just(er),
							_indexWriterEntityClient
								.getEntities(stringObjectMap)
								.map(candidates ->
									cleanCandidates(er, candidates)
								),
							Map::entry);
					}
				)
				.collectMap(Map.Entry::getKey, Map.Entry::getValue);

		return entityMap.flatMapMany(map -> {

			List<Mono<Tuple2<EntityRequest, DocumentEntityResponse>>> result = new ArrayList<>();

			for (Map.Entry<EntityRequest, List<DocumentEntityResponse>> entry : map.entrySet()) {

				EntityRequest currentEntityRequest = entry.getKey();

				List<DocumentEntityResponse> entityRequestListWithoutCurrentEntityRequest =
					map
						.entrySet()
						.stream()
						.filter(entityRequestListEntry ->
							entityRequestListEntry.getKey() != currentEntityRequest)
						.map(Map.Entry::getValue)
						.flatMap(Collection::stream)
						.collect(Collectors.toList());

				Mono<Tuple2<EntityRequest, DocumentEntityResponse>>
					entityRequestDocumentEntityMono;

				if (_log.isInfoEnabled()) {
					_log.info("doing disambiguation");
				}

				entityRequestDocumentEntityMono =
					_disambiguate(
						entry.getValue(),
						entityRequestListWithoutCurrentEntityRequest,
						tenantId, currentEntityRequest);

				result.add(entityRequestDocumentEntityMono);

			}

			return Flux.concat(result);

		})
			.collectMap(Tuple2::getT1, Tuple2::getT2)
			.flatMap(map -> {

				List<Statement> statementList = new ArrayList<>();

				for (Map.Entry<EntityRequest, DocumentEntityResponse> entrySet : map.entrySet()) {

					List<RelationRequest> relations =
						entrySet.getKey().getRelations();

					if (relations == null || relations.isEmpty()) {
						continue;
					}

					DocumentEntityResponse currentEntity = entrySet.getValue();

					List<Tuple2<String, DocumentEntityResponse>> entityRelations =
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

								DocumentEntityResponse entityRelation = t2.getT2();

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
						.then(Mono.just(ResponseList.of(request.getIngestionId(), response)));
				}
				else if (statementList.size() == 1) {
					return _graphClient
						.write(statementList.get(0))
						.then(Mono.just(ResponseList.of(request.getIngestionId(), response)));
				}
				else {
					return Mono.just(ResponseList.of(request.getIngestionId(), response));
				}

			});
	}

	private List<DocumentEntityResponse> cleanCandidates(
		EntityRequest entityRequest,
		List<DocumentEntityResponse> candidates) {

		if (_log.isDebugEnabled()) {
			_log.debug(
				"entity " + entityRequest.getName() + " candidates: " + candidates);
		}

		if (!candidates.isEmpty()) {

			DocumentEntityResponse documentEntityResponse = candidates.get(0);

			double bestScore;

			if (candidates.size() > 1) {

				if (_log.isDebugEnabled()) {
					_log.debug("softmax");
				}

				double[] scores = candidates
					.stream()
					.mapToDouble(DocumentEntityResponse::getScore)
					.toArray();

				bestScore = _softmax(documentEntityResponse.getScore(), scores);

			}
			else {

				if (_log.isDebugEnabled()) {
					_log.debug("levenshtein");
				}

				bestScore = _levenshteinDistance(
					_entityNameCleanerProvider
						.getEntityNameCleaner(documentEntityResponse.getType())
						.cleanEntityName(documentEntityResponse.getName()),
					_entityNameCleanerProvider
						.getEntityNameCleaner(entityRequest.getType())
						.cleanEntityName(entityRequest.getName())
				);

			}

			if (_log.isDebugEnabled()) {
				_log.debug(
					"current score: " + bestScore + " score threshold: " + _scoreThreshold + " for entity " + entityRequest.getName());
			}

			if (bestScore > _scoreThreshold) {
				_log.debug(
					"filtered with treshold");
				return Collections.singletonList(documentEntityResponse);
			}

		}

		if (candidates.isEmpty() && _log.isDebugEnabled()) {
			_log.debug("candidates empty");
		}

		return candidates;

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

	private Mono<Tuple2<EntityRequest, DocumentEntityResponse>> _disambiguate(
		List<DocumentEntityResponse> candidates, List<DocumentEntityResponse> entityRequestList,
		long tenantId, EntityRequest currentEntityRequest) {

		Flux<Entity> entityFlux = Flux.empty();

		String currentEntityRequestType = currentEntityRequest.getType();

		_log.info(Arrays.toString(_uniqueEntities));

		if (!candidates.isEmpty() && !_containsValue(_uniqueEntities, currentEntityRequestType)) {

			if (_log.isInfoEnabled()) {
				_log.info("disambiguating with search entity with type " + currentEntityRequestType);
			}

			Statement[] statements = new Statement[entityRequestList.size()];

			for (int i = 0; i < entityRequestList.size(); i++) {

				DocumentEntityResponse entityRequest = entityRequestList.get(i);

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
						literalOf(_labelFilter), literalOf(_minHops), literalOf(_maxHops))
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

		if (candidates.size() == 1 && _containsValue(_uniqueEntities, currentEntityRequestType)) {

			if (_log.isInfoEnabled()) {
				_log.info("disambiguating entity with type " + currentEntityRequestType);
			}

			DocumentEntityResponse candidate = candidates.get(0);

			entityFlux = _entityGraphRepository.getEntity(
				candidate.getId()).flux();
		}

		return entityFlux
			.filter(entity -> candidates
				.stream()
				.anyMatch(entity1 -> entity1.getId() == entity.getId()))
			.next()
			.switchIfEmpty(
				Mono.defer(() ->
					Mono.zip(
						_entityGraphRepository.addEntity(
							tenantId, currentEntityRequest.getName(),
							currentEntityRequest.getType()),
						_indexWriterEntityClient
							.insertEntity(
								DocumentEntityRequest
									.builder()
									.build())
						, (entity, unused) -> entity
					)
				)
			)
			.map(entity -> Tuples.of(
				currentEntityRequest,
				DocumentEntityResponse
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


	private boolean _containsValue(String[] array, String value) {
		for (String entry : array) {
			if (value.equals(entry)) {
				return true;
			}
		}
		return false;
	}

	private double _scoreThreshold;

	private int _minHops;

	private int _maxHops;

	private String[] _uniqueEntities;

	private String _labelFilter;

	@Reference
	private EntityManagerRequestConsumer _entityManagerRequestConsumer;

	@Reference
	private EntityManagerResponsePublisher _entityManagerResponsePublisher;

	@Reference
	private IndexWriterEntityClient _indexWriterEntityClient;

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
		GetOrAddEntitiesConsumer.class);

}

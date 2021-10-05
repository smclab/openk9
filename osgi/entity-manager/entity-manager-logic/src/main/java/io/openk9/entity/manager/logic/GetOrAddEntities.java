	package io.openk9.entity.manager.logic;


import io.openk9.common.api.reactor.util.ReactorStopWatch;
import io.openk9.entity.manager.api.EntityGraphRepository;
import io.openk9.entity.manager.api.EntityNameCleaner;
import io.openk9.entity.manager.api.EntityNameCleanerProvider;
import io.openk9.entity.manager.model.BaseEntity;
import io.openk9.entity.manager.model.DocumentEntity;
import io.openk9.entity.manager.model.Entity;
import io.openk9.entity.manager.model.payload.EntityRequest;
import io.openk9.entity.manager.model.payload.RelationRequest;
import io.openk9.entity.manager.model.payload.Request;
import io.openk9.entity.manager.model.payload.Response;
import io.openk9.entity.manager.model.payload.ResponseList;
import io.openk9.entity.manager.pub.sub.api.MessageRequest;
import io.openk9.index.writer.entity.client.api.IndexWriterEntityClient;
import io.openk9.index.writer.entity.model.DocumentEntityRequest;
import io.openk9.index.writer.entity.model.DocumentEntityResponse;
import io.openk9.relationship.graph.api.client.GraphClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import reactor.core.publisher.MonoSink;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.neo4j.cypherdsl.core.Cypher.literalOf;

@Component(
	immediate = true,
	service = GetOrAddEntities.class
)
public class GetOrAddEntities {

	@interface Config {
		double scoreThreshold() default 0.9;
		int minHops() default 1;
		int maxHops() default 2;
		String[] uniqueEntities() default {"date", "organization"};
		String[] notIndexEntities() default {"document"};
		String labelFilter() default "-date";
	}

	@Activate
	@Modified
	void activate(Config config) {

		_maxHops = config.maxHops();
		_minHops = config.minHops();
		_scoreThreshold = config.scoreThreshold();
		_uniqueEntities = config.uniqueEntities();
		_labelFilter = config.labelFilter();
		_notIndexEntities = config.notIndexEntities();

	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class DocumentDisambiguation {
		private List<DocumentEntityResponse> currentCandidates;
		private List<DocumentEntityResponse> restCandidates;
		private StartDisambiguation.InternalDisambiguation
			internalDisambiguation;
	}

	public Mono<EntityContext> handleMessage(
		StartDisambiguation.InternalDisambiguation id) {

		MonoSink<EntityContext> emitter = id.getEmitter();

		return Mono
			.defer(() -> {

			RequestContext request = id.getRequest();

			EntityRequest current = request.getCurrent();

			List<EntityRequest> rest = request.getRest();

			long tenantId = request.getTenantId();

			EntityNameCleaner entityNameCleaner =
				_entityNameCleanerProvider.getEntityNameCleaner(current.getType());

			Map<String, Object> currentQuery =
				entityNameCleaner
					.cleanEntityName(
						tenantId, current.getName());

			Mono<List<DocumentEntityResponse>> currentCandidates =
				stopWatch(
					"current-get-candidates",
					_indexWriterEntityClient
						.getEntities(tenantId, currentQuery)
						.map(list -> cleanCandidates(current, list))
				);

			List<Mono<List<DocumentEntityResponse>>> restCandidatesMono =
				rest
					.stream()
					.map(entityRequest -> {

						Map<String, Object> query =
							_entityNameCleanerProvider
								.getEntityNameCleaner(entityRequest.getType())
								.cleanEntityName(tenantId, entityRequest.getName());

						return _indexWriterEntityClient
							.getEntities(tenantId, query)
							.map(list -> cleanCandidates(entityRequest, list));

					})
					.collect(Collectors.toList());

			Mono<List<DocumentEntityResponse>> restCandidates =
				stopWatch(
					"rest-get-candidates",
					Flux
						.concat(restCandidatesMono)
						.flatMapIterable(Function.identity())
						.distinct()
						.collectList()
				);

			Mono<DocumentDisambiguation> disambiguateDtoMono =
				Mono.zip(
					currentCandidates,
					restCandidates,
					(t1, t2) -> DocumentDisambiguation.of(t1, t2, id)
				);


			return disambiguateDtoMono
				.flatMap(
					documentDisambiguation -> stopWatch(
						"single-disambiguate",
						_disambiguate(
							documentDisambiguation.getCurrentCandidates(),
							documentDisambiguation.getRestCandidates(),
							tenantId,
							documentDisambiguation.getInternalDisambiguation().getRequest().getCurrent()
						)
					)
				)
				.map(entity ->
					EntityContext
						.builder()
						.entity(entity)
						.entityRequest(current)
						.build())
				.doOnNext(emitter::success)
				.doOnError(emitter::error);

		})
			.contextWrite(emitter.currentContext());

	}

	private Mono<Entity> _disambiguate(
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
					_entityGraphRepository.addEntity(
						tenantId, currentEntityRequest.getName(),
						currentEntityRequest.getType()
					)
						.flatMap(entity -> {

							if (_containsValue(_notIndexEntities, entity.getType())) {
								return Mono.just(entity);
							}
							else {
								return _indexWriterEntityClient
									.insertEntity(
										DocumentEntityRequest
											.builder()
											.tenantId(entity.getTenantId())
											.name(entity.getName())
											.type(entity.getType())
											.id(entity.getId())
											.build())
									.thenReturn(entity);
							}

						})
				)
			);

	}

	public Mono<ResponseList> handleMessage(MessageRequest messageRequest) {

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
							stopWatch(
								"getEntities",
								_indexWriterEntityClient
									.getEntities(tenantId, stringObjectMap)
									.map(candidates ->
										cleanCandidates(er, candidates)
									)
							),
							Map::entry);
					}
				)
				.collectMap(Map.Entry::getKey, Map.Entry::getValue);

		return entityMap
			.flatMapMany(map -> {

				boolean searchEntities =
					map
						.entrySet()
						.stream()
						.anyMatch(
							entry ->
								!_containsValue(
									_uniqueEntities, entry.getKey().getType())
								&& !entry.getValue().isEmpty());

				Mono<List<Entity>> entitiesMono;

				if (searchEntities) {

					List<DocumentEntityResponse> candidates =
						map
							.values()
							.stream()
							.flatMap(Collection::stream)
							.distinct()
							.collect(Collectors.toList());

					entitiesMono = _getEntities(candidates).collectList().cache();
				}
				else {
					entitiesMono = Mono.empty();
				}

				List<Mono<BaseEntity>> result = new ArrayList<>();

			for (Map.Entry<EntityRequest, List<DocumentEntityResponse>> entry : map.entrySet()) {

				EntityRequest currentEntityRequest = entry.getKey();

				Mono<BaseEntity> entityRequestDocumentEntityMono;

				List<DocumentEntityResponse> internalCandidates = entry.getValue();

				if (_log.isInfoEnabled()) {
					_log.info("doing disambiguation");
				}

				if (internalCandidates.size() == 1 && _containsValue(_uniqueEntities, currentEntityRequest.getType())) {

					entityRequestDocumentEntityMono =
						_entityGraphRepository
							.getEntity(internalCandidates.get(0).getId())
							.map(List::of)
							.map(entities -> _insertToDataset(
								internalCandidates,
								entities,
								tenantId, currentEntityRequest
								)
							);

				}
				else {
					entityRequestDocumentEntityMono =
						entitiesMono
							.defaultIfEmpty(List.of())
							.map(entities -> _insertToDataset(
								internalCandidates,
								entities,
								tenantId, currentEntityRequest)
							);
				}

				result.add(entityRequestDocumentEntityMono);

			}

			return Flux.concat(result);

		})
			.collectList()
			.flatMap(baseEntityList ->
				stopWatch("write-in-index-writer-and-neo4j", Mono.defer(() -> {

					Map<Boolean, List<BaseEntity>> partitionMap =
						baseEntityList
							.stream()
							.collect(
								Collectors.partitioningBy(BaseEntity::isExistEntity));

					List<BaseEntity> notExistEntities =
						partitionMap.getOrDefault(false, List.of());

					List<Entity> entitiesToAdd
						= notExistEntities
							.stream()
							.map(baseEntity ->
								Entity
									.builder()
									.name(
										baseEntity.getEntityRequest().getName())
									.tenantId(baseEntity.getTenantId())
									.type(
										baseEntity.getEntityRequest().getType())
									.build()
							)
							.collect(Collectors.toList());

					Flux<BaseEntity> addedEntities =
						stopWatch(
							"insert-in-neo4j",
							_entityGraphRepository
								.addEntities(entitiesToAdd)
						)
						.concatMap(entity ->
							Mono.justOrEmpty(
								notExistEntities
								.stream()
								.filter(baseEntity ->
									baseEntity.getTenantId() == entity.getTenantId() &&
									baseEntity.getEntityRequest().getName().equals(entity.getName()) &&
									baseEntity.getEntityRequest().getType().equals(entity.getType()))
								.findFirst()
								.map(baseEntity -> BaseEntity.exist(baseEntity.getEntityRequest(), baseEntity.getTenantId(), entity, null))
							)
						);

					return addedEntities
						.cast(BaseEntity.ExistEntity.class)
						.collectList()
						.flatMap(entitiesToBeIndexed -> {
								List<DocumentEntityRequest> filtered =
									entitiesToBeIndexed
										.stream()
										.filter(entity -> !_containsValue(
											_notIndexEntities,
											entity.getEntity().getType()))
										.map(baseEntity -> DocumentEntityRequest
											.builder()
											.type(baseEntity.getEntity().getType())
											.tenantId(baseEntity.getTenantId())
											.name(baseEntity.getEntity().getName())
											.id(baseEntity.getEntity().getId())
											.build()
										)
										.collect(Collectors.toList());

								if (!filtered.isEmpty()) {

									if (_log.isInfoEnabled()) {
										_log.info(
											"_indexWriterEntityClient.insertEntities: " +
											filtered
												.stream()
												.map(DocumentEntityRequest::getName)
												.collect(Collectors.joining(", ")));
									}

									return stopWatch("index-writer-insertEntities", _indexWriterEntityClient.insertEntities(filtered))
										.thenReturn(entitiesToBeIndexed);
								}

								return Mono.just(entitiesToBeIndexed);
							}

						)
						.flatMapIterable(Function.identity())
						.concatWith(
							Flux
								.fromIterable(partitionMap.getOrDefault(true, List.of()))
								.cast(BaseEntity.ExistEntity.class))
						.collectList()
						.flatMap(existEntities -> stopWatch("write-relations-neo4j", Mono.defer(() -> {

							List<Statement> statementList = new ArrayList<>();

							for (BaseEntity.ExistEntity entity : existEntities) {

								List<RelationRequest> relations =
									entity.getEntityRequest().getRelations();

								if (relations == null || relations.isEmpty()) {
									continue;
								}

								List<Tuple2<String, BaseEntity.ExistEntity>> entityRelations =
									existEntities
										.stream()
										.flatMap(entry -> {

											for (RelationRequest relation : relations) {
												if (entry.getEntityRequest().getTmpId() == relation.getTo()) {
													return Stream.of(
														Tuples.of(
															relation.getName(),
															entry)
													);
												}
											}

											return Stream.empty();

										})
										.collect(Collectors.toList());

								Node currentEntityNode =
									Cypher
										.node(entity.getEntity().getType())
										.named("a");

								List<Statement> currentStatementList =
									entityRelations
										.stream()
										.map(t2 -> {

											BaseEntity.ExistEntity existEntity =
												t2.getT2();

											Entity entityRelation =
												existEntity.getEntity();


											Node entityRelationNode =
												Cypher
													.node(entityRelation.getType())
													.named("b");

											return Cypher
												.match(currentEntityNode, entityRelationNode)
												.where(
													Functions
														.id(currentEntityNode)
														.eq(literalOf(entity.getEntity().getId()))
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

							List<Response> response = existEntities
								.stream()
								.map(existEntity -> Response
									.builder()
									.entity(
										Entity
											.builder()
											.name(
												existEntity.getEntity().getName())
											.id(existEntity.getEntity().getId())
											.tenantId(
												existEntity.getEntity().getTenantId())
											.type(
												existEntity.getEntity().getType())
											.build()
									)
									.tmpId(
										existEntity.getEntityRequest().getTmpId())
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

						})));

				}))
			);
	}

	public static <T> Mono<T> stopWatch(String message, Mono<T> request) {

		if (_log.isDebugEnabled()) {
			return ReactorStopWatch.stopWatch(
				request, message, _log::debug);
		}

		return request;

	}

	public static <T> Flux<T> stopWatch(String message, Flux<T> request) {

		if (_log.isDebugEnabled()) {
			return ReactorStopWatch.stopWatch(
				request, message, _log::debug);
		}

		return request;

	}

	private Flux<Entity> _getEntities(List<DocumentEntityResponse> candidates) {

		Statement[] statements = new Statement[candidates.size()];

		for (int i = 0; i < candidates.size(); i++) {

			DocumentEntityResponse entityRequest = candidates.get(i);

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
					literalOf(_labelFilter), literalOf(_minHops),
					literalOf(_maxHops))
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

			return _entityGraphRepository.getEntities(
				entityRequestListStatement);

		}
		else if (statements.length > 1) {
			Statement entityRequestListStatement =
				Cypher
					.call(Cypher.unionAll(statements))
					.returning(NODE, HOPS)
					.orderBy(Cypher.name(HOPS))
					.build();

			return _entityGraphRepository.getEntities(
				entityRequestListStatement);
		}

		return Flux.empty();

	}

	private BaseEntity _insertToDataset(
		List<DocumentEntityResponse> candidates, List<Entity> entityList,
		long tenantId, EntityRequest currentEntityRequest) {

		Optional<Entity> first =
			entityList
				.stream()
				.filter(entity -> candidates
					.stream()
					.anyMatch(entity1 -> entity1.getId() == entity.getId()))
				.findFirst();

		if (first.isEmpty()) {
			return BaseEntity.notExist(currentEntityRequest, tenantId);
		}
		else {

			Entity entity = first.get();

			return BaseEntity.exist(
				currentEntityRequest,
				tenantId,
				entity,
				DocumentEntity
					.builder()
					.tenantId(entity.getTenantId())
					.type(entity.getType())
					.name(entity.getName())
					.id(entity.getId())
					.score(1L)
					.build());

		}

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

	private String[] _notIndexEntities;

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
		GetOrAddEntities.class);

}

package io.openk9.entity.manager.jet;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import io.openk9.entity.manager.action.GetEntitiesCallable;
import io.openk9.entity.manager.cache.model.AssociableEntityKey;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cleaner.EntityNameCleaner;
import io.openk9.entity.manager.cleaner.EntityNameCleanerProvider;
import io.openk9.entity.manager.config.EntityGraphConfig;
import io.openk9.entity.manager.model.graph.EntityGraph;
import io.openk9.entity.manager.model.index.EntityIndex;
import io.openk9.entity.manager.service.graph.EntityGraphService;
import io.openk9.entity.manager.service.index.EntityService;
import io.openk9.entity.manager.util.FutureUtil;
import io.openk9.entity.manager.util.MapUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.index.query.QueryBuilder;
import org.jboss.logging.Logger;
import org.neo4j.cypherdsl.core.AliasedExpression;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Functions;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Property;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.SymbolicName;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.neo4j.cypherdsl.core.Cypher.literalOf;

public class CreateEntitiesRunnable
	implements StopWatchRunnable, HazelcastInstanceAware, Serializable {

	@Override
	public void run_() {

		_log.info("start CreateEntitiesRunnable");

		IMap<EntityKey, Entity> entityIMap =
			MapUtil.getEntityMap(_hazelcastInstance);

		IMap<AssociableEntityKey, Entity> associableEntityMap =
			MapUtil.getAssociableEntityMap(_hazelcastInstance);

		Set<EntityKey> entityKeys = entityIMap.localKeySet(
			Predicates.and(
				Predicates.equal("id", null),
				Predicates.equal("graphId", null)
			)
		);

		EntityGraphConfig config =
			CDI.current().select(EntityGraphConfig.class).get();

		EntityNameCleanerProvider entityNameCleanerProvider =
			CDI.current().select(EntityNameCleanerProvider.class).get();

		EntityService entityService =
			CDI.current().select(EntityService.class).get();

		EntityGraphService entityGraphService = CDI.current().select(
			EntityGraphService.class).get();

		Map<EntityKey, Entity> localEntityMap = entityIMap.getAll(entityKeys);

		Collection<Entity> localEntityValues = localEntityMap.values();

		Set<EntityKey> localEntityKeys = localEntityMap.keySet();

		List<Member> collect =
			_hazelcastInstance
				.getCluster()
				.getMembers()
				.stream()
				.filter(member -> !member.localMember())
				.collect(Collectors.toList());

		String[] ingestionIds =
			localEntityKeys
				.stream()
				.map(EntityKey::getIngestionId)
				.distinct()
				.toArray(String[]::new);

		IExecutorService entityExecutor =
			_hazelcastInstance.getExecutorService("entityExecutor");

		Map<Member, Future<Map<EntityKey, Entity>>> memberFutureMap =
			entityExecutor.submitToMembers(
				new GetEntitiesCallable(ingestionIds), collect);

		Map<EntityKey, Entity> otherEntityKeyEntityMap =
			memberFutureMap
				.values()
				.stream()
				.map(FutureUtil::makeCompletableFuture)
				.map(CompletableFuture::join)
				.reduce((a, b) -> {

					Map<EntityKey, Entity> map = new HashMap<>();

					map.putAll(a);
					map.putAll(b);

					return map;

				})
				.orElseGet(Map::of);

		Stream<EntityMember> otherEntityMemberStream =
			otherEntityKeyEntityMap
				.values()
				.stream()
				.map(entity -> EntityMember.of(entity, false));

		Stream<EntityMember> localEntityMemberStream = localEntityValues
			.stream()
			.map(entity -> EntityMember.of(entity, true));

		Map<String, List<EntityMember>> entitiesGroupingByIngestionId =
			Stream.concat(localEntityMemberStream, otherEntityMemberStream)
				.collect(Collectors.groupingBy(
					entityMember -> entityMember.getEntity().getIngestionId()));

		Collection<List<EntityMember>> values =
			entitiesGroupingByIngestionId.values();

		Map<EntityKey, Entity> entityMap = new HashMap<>();

		for (List<EntityMember> ingestionIdEntities : values) {

			Map<AssociableEntityKey, Entity> localAssociableEntityMap = new HashMap<>();

			List<EntityCandidates> entityCandidateList = new ArrayList<>();

			for (EntityMember ingestionIdEntity : ingestionIdEntities) {

				Entity innerEntity = ingestionIdEntity.getEntity();

				entityCandidateList.add(
					getEntityCandidates(
						entityNameCleanerProvider,
						entityService,
						ingestionIdEntity, innerEntity)
				);

			}

			List<Mono<Entity>> completableFutureList =
				entityCandidateList
					.stream()
					.filter(
						entityCandidates -> entityCandidates.getEntity().isLocal())
					.map(entityCandidates ->
							Mono.fromSupplier(
							_getAndCreateEntityDisambiguate(
								config, entityNameCleanerProvider, entityService,
								entityGraphService,
								entityCandidateList, entityCandidates,
								entityCandidates.getEntity()
							)
						).subscribeOn(Schedulers.boundedElastic())
					)
					.collect(Collectors.toList());

			Mono<List<Entity>> zip =
				Mono.zip(completableFutureList, a -> {
					List<Entity> entities = new ArrayList<>();
					for (Object o : a) {
						entities.add((Entity)o);
					}
					return entities;
				})
					.defaultIfEmpty(List.of());

			for (Entity currentEntityRequest : zip.block()) {

				localAssociableEntityMap.put(
					AssociableEntityKey.of(
						currentEntityRequest.getCacheId(),
						currentEntityRequest.getIngestionId()
					), currentEntityRequest);

				entityMap.put(
					EntityKey.of(
						currentEntityRequest.getTenantId(),
						currentEntityRequest.getName(),
						currentEntityRequest.getType(),
						currentEntityRequest.getCacheId(),
						currentEntityRequest.getIngestionId()
					), currentEntityRequest);
			}

			associableEntityMap.setAll(localAssociableEntityMap);

		}

		entityIMap.setAll(entityMap);

	}

	private Supplier<Entity> _getAndCreateEntityDisambiguate(
		EntityGraphConfig config,
		EntityNameCleanerProvider entityNameCleanerProvider,
		EntityService entityService, EntityGraphService entityGraphService,
		List<EntityCandidates> entityCandidateList,
		EntityCandidates ingestionIdEntityCandidate,
		EntityMember ingestionIdEntityMember) {

		return () -> {

			if (_log.isDebugEnabled()) {
				_log.debug(
					"current: " + ingestionIdEntityMember);
			}

			Entity currentEntityRequest =
				ingestionIdEntityMember.getEntity();

			Entity copy = new Entity(
				currentEntityRequest.getId(),
				currentEntityRequest.getCacheId(),
				currentEntityRequest.getTenantId(),
				currentEntityRequest.getName(),
				currentEntityRequest.getType(),
				currentEntityRequest.getGraphId(),
				currentEntityRequest.getIngestionId(),
				currentEntityRequest.isAssociated(),
				currentEntityRequest.isIndexable(),
				currentEntityRequest.getContext());

			List<EntityIndex> restCandidates =
				entityCandidateList
					.stream()
					.filter(entity -> entity !=
									  ingestionIdEntityCandidate)
					.map(entityCandidates ->
						cleanCandidates(
							entityCandidates.getEntity().getEntity(),
							entityCandidates.getCandidates(),
							entityNameCleanerProvider,
							config.getScoreThreshold()
						)
					)
					.flatMap(Collection::stream)
					.collect(Collectors.toList());

			Optional<EntityGraph> optionalEntityGraph =
				_disambiguate(
					entityGraphService,
					cleanCandidates(
						copy,
						ingestionIdEntityCandidate.getCandidates(),
						entityNameCleanerProvider,
						config.getScoreThreshold()),
					restCandidates,
					copy,
					config.getUniqueEntities(),
					config.getMinHops(),
					config.getMaxHops());

			if (optionalEntityGraph.isEmpty()) {
				try {

					EntityGraph entityGraph =
						entityGraphService.insertEntity(
							copy.getType(),
							EntityGraph.of(
								copy.getCacheId(),
								null,
								copy.getTenantId(),
								copy.getName(),
								copy.getType()
							)
						);

					copy.setGraphId(
						entityGraph.getGraphId());

					if (copy.isIndexable()) {

						entityService.awaitIndex(
							EntityIndex.of(
								copy.getCacheId(),
								entityGraph.getGraphId(),
								copy.getTenantId(),
								copy.getName(),
								copy.getType(),
								0)
						);

					}

					copy.setId(copy.getCacheId());

				}
				catch (Exception ioe) {
					_log.error(ioe.getMessage());
				}
			}
			else {

				EntityGraph entityGraph =
					optionalEntityGraph.get();

				copy.setId(entityGraph.getId());
				copy.setGraphId(
					entityGraph.getGraphId());

			}

			return copy;

		};
	}

	private EntityCandidates getEntityCandidates(
		EntityNameCleanerProvider entityNameCleanerProvider,
		EntityService entityService, EntityMember ingestionIdEntityMember,
		Entity ingestionIdEntity) {

		EntityNameCleaner entityNameCleaner =
			entityNameCleanerProvider.get(
				ingestionIdEntity.getType());

		QueryBuilder queryBuilder =
			entityNameCleaner.cleanEntityName(
				ingestionIdEntity.getTenantId(),
				ingestionIdEntity.getName());

		List<EntityIndex> candidates =
			entityService.search(
				ingestionIdEntity.getTenantId(), queryBuilder, 0, 10);

		return EntityCandidates.of(ingestionIdEntityMember, candidates);
	}

	private List<EntityIndex> cleanCandidates(
		Entity entityRequest,
		List<EntityIndex> candidates,
		EntityNameCleanerProvider entityNameCleanerProvider,
		float scoreThreshold) {

		if (!candidates.isEmpty()) {

			EntityIndex documentEntityResponse = candidates.get(0);

			double bestScore;

			if (candidates.size() > 1) {

				double[] scores = candidates
					.stream()
					.mapToDouble(EntityIndex::getScore)
					.toArray();

				bestScore = _softmax(documentEntityResponse.getScore(), scores);

			}
			else {

				bestScore = _levenshteinDistance(
					entityNameCleanerProvider
						.get(documentEntityResponse.getType())
						.cleanEntityName(documentEntityResponse.getName()),
					entityNameCleanerProvider
						.get(entityRequest.getType())
						.cleanEntityName(entityRequest.getName())
				);

			}

			if (bestScore > scoreThreshold) {
				return Collections.singletonList(documentEntityResponse);
			}

		}

		return candidates;

	}

	private Optional<EntityGraph> _disambiguate(
		EntityGraphService entityGraphService,
		List<EntityIndex> candidates, List<EntityIndex> entityRequestList,
		Entity currentEntityRequest, String[] uniqueEntities, int minHops,
		int maxHops) {

		String currentEntityRequestType = currentEntityRequest.getType();

		List<EntityGraph> result = List.of();

		if (_containsValue(uniqueEntities, currentEntityRequestType)) {
			if (candidates.size() == 1) {

				if (_log.isDebugEnabled()) {
					_log.debug("disambiguating entity with type " + currentEntityRequestType);
				}

				EntityIndex candidate = candidates.get(0);

				EntityGraph entityGraph =
					EntityGraph.of(
						candidate.getId(),
						candidate.getGraphId(),
						candidate.getTenantId(),
						candidate.getName(),
						candidate.getType()
					);

				result = List.of(entityGraph);
			}
			else if (candidates.size() > 1) {

				result = _getEntityGraphs(
					entityGraphService, entityRequestList,
					minHops, maxHops, currentEntityRequestType, result);

			}
		}
		else {
			if (!candidates.isEmpty()) {

				result = _getEntityGraphs(
					entityGraphService, entityRequestList,
					minHops, maxHops, currentEntityRequestType, result);

			}
		}

		if (_log.isDebugEnabled()) {
			_log.debug("_disambiguate: " + result + " current: " + currentEntityRequest);
		}

		return
			result
				.stream()
				.filter(Objects::nonNull)
				.filter(entityGraph ->
						candidates
							.stream()
							.anyMatch(
								entityIndex ->
									entityGraph.getId().equals(entityIndex.getId())))
				.findFirst();

	}

	private List<EntityGraph> _getEntityGraphs(
		EntityGraphService entityGraphService,
		List<EntityIndex> entityRequestList, int minHops, int maxHops,
		String currentEntityRequestType, List<EntityGraph> result) {

		if (_log.isDebugEnabled()) {
			_log.debug("disambiguating with search entity with type " +
					   currentEntityRequestType);
		}

		Statement[] statements = new Statement[entityRequestList.size()];

		for (int i = 0; i < entityRequestList.size(); i++) {

			EntityIndex entityRequest = entityRequestList.get(i);

			Node nodeEntity =
				Cypher.node(entityRequest.getType()).named("entity");

			AliasedExpression entityAliased = nodeEntity.as("entity");

			SymbolicName path = Cypher.name("path");

			Property idProperty = entityAliased
				.getDelegate()
				.property("id");

			Statement statement = Cypher
				.match(nodeEntity)
				.where(idProperty.eq(literalOf(entityRequest.getId())))
				.call("apoc.path.expand").withArgs(
					entityAliased.getDelegate(), literalOf(null),
					literalOf("-date"), literalOf(minHops), literalOf(maxHops))
				.yield(path)
				.returning(
					Functions.last(Functions.nodes(path)).as("node"),
					Functions.size(Functions.nodes(path)).subtract(
						literalOf(1)).as("hops"))
				.build();

			statements[i] = statement;

		}

		if (statements.length == 1) {
			Statement entityRequestListStatement =
				Cypher
					.call(statements[0])
					.returning("node", "hops")
					.orderBy(Cypher.name("hops"))
					.build();

			result = entityGraphService.search(
				entityRequestListStatement);

		}
		else if (statements.length > 1) {
			Statement entityRequestListStatement =
				Cypher
					.call(Cypher.unionAll(statements))
					.returning("node", "hops")
					.orderBy(Cypher.name("hops"))
					.build();

			result = entityGraphService.search(
				entityRequestListStatement);
		}
		return result;
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

	@Override
	public void setHazelcastInstance(
		HazelcastInstance hazelcastInstance) {
		_hazelcastInstance = hazelcastInstance;
	}

	@Data
	@Builder
	@AllArgsConstructor(staticName = "of")
	@ToString
	public static class EntityMember {
		private final Entity entity;
		private final boolean local;
	}

	@Data
	@Builder
	@AllArgsConstructor(staticName = "of")
	@ToString
	public static class EntityIngestionContext {
		private final EntityMember current;
		private final List<EntityMember> rest;
	}

	@Data
	@Builder
	@AllArgsConstructor(staticName = "of")
	@ToString
	public static class EntityCandidates {
		private final EntityMember entity;
		private final List<EntityIndex> candidates;
	}

	private transient HazelcastInstance _hazelcastInstance;

	private static final Logger _log = Logger.getLogger(
		CreateEntitiesRunnable.class);

}

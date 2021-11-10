package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cleaner.EntityNameCleaner;
import io.openk9.entity.manager.cleaner.EntityNameCleanerProvider;
import io.openk9.entity.manager.config.EntityGraphConfig;
import io.openk9.entity.manager.model.graph.EntityGraph;
import io.openk9.entity.manager.model.index.EntityIndex;
import io.openk9.entity.manager.service.graph.EntityGraphService;
import io.openk9.entity.manager.service.index.EntityService;
import io.openk9.entity.manager.util.MapUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.elasticsearch.index.query.QueryBuilder;
import org.jboss.logging.Logger;
import org.neo4j.cypherdsl.core.AliasedExpression;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Functions;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Property;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.SymbolicName;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.neo4j.cypherdsl.core.Cypher.literalOf;

public class CreateEntitiesRunnable
	implements StopWatchRunnable, HazelcastInstanceAware, Serializable {

	@Override
	public void run_() {

		_log.info("start CreateEntitiesRunnable");

		IMap<EntityKey, Entity> entityIMap =
			MapUtil.getEntityMap(_hazelcastInstance);

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

		Collection<Entity> entityMapValues = localEntityMap.values();

		Map<String, List<Entity>> entitiesGroupingByIngestionId =
			entityMapValues
				.stream()
				.collect(Collectors.groupingBy(Entity::getIngestionId));

		Collection<List<Entity>> values =
			entitiesGroupingByIngestionId.values();

		for (List<Entity> ingestionIdEntities : values) {

			List<EntityCandidates> entityCandidates =
				new ArrayList<>(ingestionIdEntities.size());

			for (Entity ingestionIdEntity : ingestionIdEntities) {

				EntityNameCleaner entityNameCleaner =
					entityNameCleanerProvider.get(
						ingestionIdEntity.getType());

				QueryBuilder queryBuilder =
					entityNameCleaner.cleanEntityName(
						ingestionIdEntity.getTenantId(),
						ingestionIdEntity.getName());

				try {

					List<EntityIndex> candidates =
						entityService.search(
							ingestionIdEntity.getTenantId(), queryBuilder, 0, 10);

					entityCandidates.add(
						EntityCandidates.of(ingestionIdEntity, candidates));

				}
				catch (Exception e) {
					_log.error(e.getMessage());
				}

			}

			for (EntityCandidates current : entityCandidates) {

				Entity currentEntityRequest = current.getEntity();

				List<EntityIndex> restCandidates =
					entityCandidates
						.stream()
						.filter(entity -> entity != current)
						.map(EntityCandidates::getCandidates)
						.flatMap(Collection::stream)
						.collect(Collectors.toList());

				Optional<EntityGraph> optionalEntityGraph =
					_disambiguate(
						entityGraphService,
						cleanCandidates(
							currentEntityRequest, current.getCandidates(),
							entityNameCleanerProvider,
							config.getScoreThreshold()),
						cleanCandidates(
							currentEntityRequest, restCandidates,
							entityNameCleanerProvider,
							config.getScoreThreshold()),
						currentEntityRequest,
						config.getUniqueEntities(), config.getMinHops(),
						config.getMaxHops());

				if (optionalEntityGraph.isEmpty()) {
					try {
						entityService.index(
							EntityIndex.of(
								currentEntityRequest.getCacheId(),
								currentEntityRequest.getTenantId(),
								currentEntityRequest.getName(),
								currentEntityRequest.getType(),
								0)
						);

						currentEntityRequest.setId(currentEntityRequest.getCacheId());

						EntityGraph entityGraph =
							entityGraphService.insertEntity(
								currentEntityRequest.getType(),
								EntityGraph.of(
									currentEntityRequest.getId(),
									null,
									currentEntityRequest.getTenantId(),
									currentEntityRequest.getName(),
									currentEntityRequest.getType()
								)
							);

						currentEntityRequest.setGraphId(entityGraph.getGraphId());

					}
					catch (Exception ioe) {
						_log.error(ioe.getMessage());
					}
				}
				else {

					EntityGraph entityGraph = optionalEntityGraph.get();

					currentEntityRequest.setId(entityGraph.getId());
					currentEntityRequest.setGraphId(entityGraph.getGraphId());

				}

				entityIMap.set(
					EntityKey.of(
						currentEntityRequest.getTenantId(),
						currentEntityRequest.getName(),
						currentEntityRequest.getType(),
						currentEntityRequest.getCacheId(),
						currentEntityRequest.getIngestionId()
					), currentEntityRequest);
			}
		}

	}

	private List<EntityIndex> cleanCandidates(
		Entity entityRequest,
		List<EntityIndex> candidates,
		EntityNameCleanerProvider entityNameCleanerProvider,
		float scoreThreshold) {

		if (_log.isDebugEnabled()) {
			_log.debug(
				"entity " + entityRequest.getName() + " candidates: " + candidates);
		}

		if (!candidates.isEmpty()) {

			EntityIndex documentEntityResponse = candidates.get(0);

			double bestScore;

			if (candidates.size() > 1) {

				if (_log.isDebugEnabled()) {
					_log.debug("softmax");
				}

				double[] scores = candidates
					.stream()
					.mapToDouble(EntityIndex::getScore)
					.toArray();

				bestScore = _softmax(documentEntityResponse.getScore(), scores);

			}
			else {

				if (_log.isDebugEnabled()) {
					_log.debug("levenshtein");
				}

				bestScore = _levenshteinDistance(
					entityNameCleanerProvider
						.get(documentEntityResponse.getType())
						.cleanEntityName(documentEntityResponse.getName()),
					entityNameCleanerProvider
						.get(entityRequest.getType())
						.cleanEntityName(entityRequest.getName())
				);

			}

			if (_log.isDebugEnabled()) {
				_log.debug(
					"current score: " + bestScore + " score threshold: " + scoreThreshold + " for entity " + entityRequest.getName());
			}

			if (bestScore > scoreThreshold) {
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

	private Optional<EntityGraph> _disambiguate(
		EntityGraphService entityGraphService,
		List<EntityIndex> candidates, List<EntityIndex> entityRequestList,
		Entity currentEntityRequest, String[] uniqueEntities, int minHops,
		int maxHops) {

		String currentEntityRequestType = currentEntityRequest.getType();

		List<EntityGraph> result = List.of();

		if (!candidates.isEmpty() && !_containsValue(uniqueEntities, currentEntityRequestType)) {

			if (_log.isDebugEnabled()) {
				_log.debug("disambiguating with search entity with type " + currentEntityRequestType);
			}

			Statement[] statements = new Statement[entityRequestList.size()];

			for (int i = 0; i < entityRequestList.size(); i++) {

				EntityIndex entityRequest = entityRequestList.get(i);

				Node nodeEntity =
					Cypher.node(entityRequest.getType()).named("entity");

				AliasedExpression entityAliased = nodeEntity.as("entity");

				SymbolicName path = Cypher.name("path");

				Property idProperty = entityAliased.property("id");

				Statement statement = Cypher
					.match(nodeEntity)
					.where(idProperty.eq(literalOf(entityRequest.getId())))
					.call("apoc.path.expand").withArgs(
						entityAliased, literalOf(null),
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

		}

		if (candidates.size() == 1 && _containsValue(uniqueEntities, currentEntityRequestType)) {

			if (_log.isDebugEnabled()) {
				_log.debug("disambiguating entity with type " + currentEntityRequestType);
			}

			EntityIndex candidate = candidates.get(0);

			result = List.of(entityGraphService.getEntity(candidate.getId()));
		}

		return
			result
				.stream()
				.filter(entityGraph ->
					entityGraph.getId() == currentEntityRequest.getId())
				.findFirst();

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
	public static class EntityIngestionContext {
		private final Entity current;
		private final List<Entity> rest;
	}

	@Data
	@Builder
	@AllArgsConstructor(staticName = "of")
	public static class EntityCandidates {
		private final Entity entity;
		private final List<EntityIndex> candidates;
	}

	private transient HazelcastInstance _hazelcastInstance;

	private static final Logger _log = Logger.getLogger(
		CreateEntitiesRunnable.class);

}

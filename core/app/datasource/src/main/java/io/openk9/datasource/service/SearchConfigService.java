/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.validation.constraints.NotNull;

import io.openk9.common.graphql.SortBy;
import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Strings;
import io.openk9.datasource.mapper.QueryParserConfigMapper;
import io.openk9.datasource.mapper.SearchConfigMapper;
import io.openk9.datasource.model.QueryAnalysis_;
import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.SearchConfig_;
import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;
import io.openk9.datasource.model.dto.base.SearchConfigDTO;
import io.openk9.datasource.model.dto.request.HybridSearchPipelineDTO;
import io.openk9.datasource.model.dto.request.SearchConfigWithQueryParsersDTO;
import io.openk9.datasource.model.dto.response.SearchPipelineResponseDTO;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.Tuple2;

import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.generic.Bodies;
import org.opensearch.client.opensearch.generic.Request;
import org.opensearch.client.opensearch.generic.Requests;


@ApplicationScoped
public class SearchConfigService extends BaseK9EntityService<SearchConfig, SearchConfigDTO> {

	private static final Logger log = Logger.getLogger(SearchConfigService.class);
	@Inject
	QueryParserConfigMapper _queryParserConfigMapper;
	@Inject
	OpenSearchClient openSearchClient;
	@Inject
	QueryParserConfigService queryParserConfigService;

	SearchConfigService(SearchConfigMapper mapper) {
		 this.mapper = mapper;
	}

	/**
	 * Renders the search pipeline body. The fields left {@code null}, as a
	 * JSON body that omits them does, take the values of
	 * {@link HybridSearchPipelineDTO#DEFAULT}.
	 */
	protected static JsonObject getJsonBody(HybridSearchPipelineDTO pipelineDTO) {
		var defaults = HybridSearchPipelineDTO.DEFAULT;

		var normalizationTechnique = Objects.requireNonNullElse(
			pipelineDTO.getNormalizationTechnique(),
			defaults.getNormalizationTechnique());
		var combinationTechnique = Objects.requireNonNullElse(
			pipelineDTO.getCombinationTechnique(),
			defaults.getCombinationTechnique());
		var weights = Objects.requireNonNullElse(
			pipelineDTO.getWeights(), defaults.getWeights());

		return Json.createObjectBuilder()
			.add("description", "Post processor for hybrid search")
			.add("phase_results_processors", Json.createArrayBuilder()
				.add(Json.createObjectBuilder()
					.add("normalization-processor", Json.createObjectBuilder()
						.add("normalization", Json.createObjectBuilder()
							.add(
								"technique",
								normalizationTechnique.getValue()
							)
						)
						.add("combination", Json.createObjectBuilder()
							.add(
								"technique",
								combinationTechnique.getValue()
							)
							.add("parameters", Json.createObjectBuilder()
								.add(
									"weights",
									Json.createArrayBuilder(weights)

								)
							)
						)
					)
				)
			)
			.build();
	}

	public Uni<Tuple2<SearchConfig, QueryParserConfig>> addQueryParserConfig(
		long id, QueryParserConfigDTO queryParserConfigDTO) {

		QueryParserConfig queryParserConfig =
			_queryParserConfigMapper.create(queryParserConfigDTO);

		return sessionFactory.withTransaction(s -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(searchConfig -> s.fetch(searchConfig.getQueryParserConfigs()).flatMap(
				queryParserConfigs -> {
					if (searchConfig.addQueryParserConfig(queryParserConfigs, queryParserConfig)) {
						return persist(s, searchConfig)
							.map(dt -> Tuple2.of(dt, queryParserConfig));
					}
					return Uni.createFrom().nullItem();
				})));
	}

	/**
	 * Writes the hybrid search pipeline of a {@link SearchConfig} with the
	 * given techniques and weights, replacing the current one. The pipeline
	 * is provisioned with the defaults when the SearchConfig is created: this
	 * is the tuning tool, and the way to provision the SearchConfigs created
	 * before that.
	 */
	public Uni<SearchPipelineResponseDTO> configureHybridSearch(
		long id, @NotNull HybridSearchPipelineDTO pipelineDTO) {

		return sessionFactory.withTransaction(s -> findById(s, id))
			.flatMap(searchConfig -> putSearchPipeline(
				searchConfig.getName(), pipelineDTO));
	}

	@Override
	public Uni<SearchConfig> deleteById(Mutiny.Session s, long entityId) {

		// flushed first: a SearchConfig still referenced fails here, before
		// its pipeline is gone
		return super.deleteById(s, entityId)
			.call(s::flush)
			.call(searchConfig -> deleteSearchPipeline(searchConfig.getName()));
	}

	/**
	 * Creates the hybrid search pipeline of a new {@link SearchConfig} with
	 * the default body. A pipeline already there under the same name is left
	 * as it is, so a tuning done through {@link #configureHybridSearch} is
	 * never overwritten. Any failure fails the creation.
	 */
	Uni<Void> provisionSearchPipeline(SearchConfig searchConfig) {

		var name = searchConfig.getName();

		return executeOnPipeline("GET", name)
			.flatMap(existing -> {
				if (existing.status() == 200) {
					return Uni.createFrom().voidItem();
				}

				if (existing.status() != 404) {
					return Uni.createFrom().failure(
						pipelineFailure("read", name, existing));
				}

				return putSearchPipeline(name, HybridSearchPipelineDTO.DEFAULT)
					.flatMap(created -> isSuccessful(created)
						? Uni.createFrom().voidItem()
						: Uni.createFrom().failure(
							pipelineFailure("create", name, created)));
			});
	}

	/**
	 * Deletes the hybrid search pipeline of a deleted {@link SearchConfig}.
	 * Best effort: the SearchConfig is gone either way, a pipeline left on
	 * the cluster is logged and does no harm.
	 */
	private Uni<Void> deleteSearchPipeline(String name) {

		return executeOnPipeline("DELETE", name)
			.invoke(response -> {
				if (!isSuccessful(response) && response.status() != 404) {
					log.warn(pipelineFailure("delete", name, response).getMessage());
				}
			})
			.onFailure()
			.recoverWithItem(failure -> {
				log.warnf(
					failure, "Cannot delete the search pipeline %s", pipelineName(name));
				return null;
			})
			.replaceWithVoid();
	}

	private Uni<SearchPipelineResponseDTO> putSearchPipeline(
		String name, HybridSearchPipelineDTO pipelineDTO) {

		return execute(Requests.builder()
			.method("PUT")
			.endpoint(pipelineEndpoint(name))
			.json(getJsonBody(pipelineDTO))
			.build()
		);
	}

	private Uni<SearchPipelineResponseDTO> executeOnPipeline(
		String method, String name) {

		return execute(Requests.builder()
			.method(method)
			.endpoint(pipelineEndpoint(name))
			.build()
		);
	}

	private Uni<SearchPipelineResponseDTO> execute(Request request) {

		return VertxContextSupport.executeBlocking(() -> {
			try (var response = openSearchClient.generic().execute(request)) {
				return new SearchPipelineResponseDTO(
					response.getStatus(),
					response.getBody().orElse(Bodies.json("{}")).bodyAsString(),
					response.getReason()
				);
			}
		});
	}

	private static String pipelineName(String searchConfigName) {
		return Strings.retainsAlnum(searchConfigName);
	}

	private static String pipelineEndpoint(String searchConfigName) {
		return "_search/pipeline/" + pipelineName(searchConfigName);
	}

	private static boolean isSuccessful(SearchPipelineResponseDTO response) {
		return response.status() >= 200 && response.status() < 300;
	}

	private static IllegalStateException pipelineFailure(
		String action, String name, SearchPipelineResponseDTO response) {

		return new IllegalStateException(String.format(
			"Cannot %s the search pipeline %s: %d %s",
			action,
			pipelineName(name),
			response.status(),
			response.body()
		));
	}

	@Override
	public Uni<SearchConfig> create(SearchConfigDTO dto) {
		if (dto instanceof SearchConfigWithQueryParsersDTO withQueryParsersDTO) {
			SearchConfig transientSearchConfig = mapper.create(withQueryParsersDTO);

			return sessionFactory.withTransaction((s, tr) ->
				super.create(s, transientSearchConfig)
					.flatMap(searchConfig -> {

						// UniBuilder to prevent empty unis
						List<Uni<Void>> unis = new ArrayList<>();
						unis.add(Uni.createFrom().voidItem());

						var queryParsers = withQueryParsersDTO.getQueryParsers();

						if (queryParsers != null) {
							// iterates all DTO's queryParser information
							unis.addAll(queryParsers.stream()
								.map(queryParserConfigDTO ->
									// creates a queryParser based on the DTO's information
									queryParserConfigService.create(s, queryParserConfigDTO)
										.invoke(queryParserConfig ->
											// binds the queryParser to the searchConfig
											searchConfig.addQueryParserConfig(
												searchConfig.getQueryParserConfigs(),
												queryParserConfig
											)
										)
										.replaceWithVoid()
								)
								.toList()
							);
						}

						return Uni.combine().all().unis(unis)
							.usingConcurrencyOf(1)
							.discardItems()
							.onFailure()
							.invoke(log::error)
							.flatMap(ignored -> s.merge(searchConfig))
							.call(s::flush)
							.call(this::provisionSearchPipeline);
					})
			);
		}
		return sessionFactory.withTransaction((s, tr) -> super.create(s, dto)
			.call(s::flush)
			.call(this::provisionSearchPipeline));
	}

	@Override
	public Class<SearchConfig> getEntityClass() {
		return SearchConfig.class;
	}

	public Uni<Set<QueryParserConfig>> getQueryParserConfig(SearchConfig searchConfig) {
		return sessionFactory.withTransaction(s -> s.fetch(searchConfig.getQueryParserConfigs()));
	}

	public Uni<Connection<QueryParserConfig>> getQueryParserConfigs(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, SearchConfig_.QUERY_PARSER_CONFIGS, QueryParserConfig.class,
			queryParserConfigService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}

	public Uni<Page<QueryParserConfig>> getQueryParserConfigs(
		long searchConfigId, Pageable pageable) {
		return getQueryParserConfigs(searchConfigId, pageable, Filter.DEFAULT);
	}

	public Uni<Page<QueryParserConfig>> getQueryParserConfigs(
		long searchConfigId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[]{searchConfigId},
			SearchConfig_.QUERY_PARSER_CONFIGS, QueryParserConfig.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			searchText);
	}

	public Uni<Page<QueryParserConfig>> getQueryParserConfigs(
		long searchConfigId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[]{searchConfigId},
			SearchConfig_.QUERY_PARSER_CONFIGS, QueryParserConfig.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			filter);
	}

	public Uni<Connection<QueryParserConfig>> getQueryParserConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {
		return findJoinConnection(
			id, SearchConfig_.QUERY_PARSER_CONFIGS, QueryParserConfig.class,
			queryParserConfigService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {QueryAnalysis_.NAME, QueryAnalysis_.DESCRIPTION};
	}

	/**
	 * Patches an existing {@link SearchConfig} by updating its fields and replacing its associated
	 * {@link QueryParserConfig}s if the provided DTO includes them.
	 *
	 * <p>If the {@code dto} is an instance of {@link SearchConfigWithQueryParsersDTO}, the method updates
	 * the main entity and fully replaces its related query parser configurations inside a transactional context.</p>
	 *
	 * @param id the ID of the {@link SearchConfig} to patch
	 * @param dto the data transfer object containing the new values (with or without query parsers)
	 * @return a {@link Uni} emitting the updated {@link SearchConfig}
	 */
	@Override
	public Uni<SearchConfig> patch(long id, SearchConfigDTO dto) {
		if (dto instanceof SearchConfigWithQueryParsersDTO withQueryParsersDTO) {
			return sessionFactory.withTransaction(s -> findById(s, id)
				.call(searchConfig ->
					Mutiny.fetch(searchConfig.getQueryParserConfigs()))
				.flatMap(searchConfig -> {

					SearchConfig newStateSearchConfig =
						mapper.patch(searchConfig, withQueryParsersDTO);

					// UniBuilder to prevent empty unis
					List<Uni<Void>> unis = new ArrayList<>();
					unis.add(Uni.createFrom().voidItem());

					List<QueryParserConfigDTO> queryParsers =
						withQueryParsersDTO.getQueryParsers();

					if (queryParsers != null) {
						searchConfig.getQueryParserConfigs().clear();

						// iterates all DTO's queryParser information
						unis.addAll(queryParsers.stream()
							.map(queryParserConfigDTO ->
								// creates a queryParser based on the DTO's information
								queryParserConfigService.create(
										s, queryParserConfigDTO)
									// binds the queryParser to the searchConfig
									.invoke(queryParserConfig ->
										searchConfig.addQueryParserConfig(
											newStateSearchConfig
												.getQueryParserConfigs(),
											queryParserConfig
										)
									)
									.replaceWithVoid()
							)
							.toList()
						);
					}

					return Uni.combine().all().unis(unis)
						.usingConcurrencyOf(1)
						.discardItems()
						.onFailure()
						.invoke(log::error)
						.flatMap(ignored ->
							s.merge(newStateSearchConfig));
				})
			);
		}
		return super.patch(id, dto);
	}

	/**
	 * Removes all {@link QueryParserConfig}s associated with the
	 * given {@link SearchConfig}. Relies on {@code orphanRemoval}
	 * to delete the rows from the database at flush time.
	 *
	 * @param s the current Hibernate reactive session
	 * @param id the ID of the {@link SearchConfig}
	 * @return a {@link Uni} emitting the updated {@link SearchConfig}
	 */
	public Uni<SearchConfig> removeAllQueryParserConfig(Mutiny.Session s, long id) {
		return findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(searchConfig -> s
				.fetch(searchConfig.getQueryParserConfigs())
				.flatMap(queryParserConfigs -> {
					searchConfig.removeAllQueryParserConfig(
						queryParserConfigs);
					return merge(s, searchConfig);
				})
			);
	}

	/**
	 * Removes all {@link QueryParserConfig}s associated with the
	 * given {@link SearchConfig} within a new transaction.
	 *
	 * @param id the ID of the {@link SearchConfig}
	 * @return a {@link Uni} emitting the updated {@link SearchConfig}
	 */
	public Uni<SearchConfig> removeAllQueryParserConfig(long id) {
		return sessionFactory.withTransaction(
			s -> removeAllQueryParserConfig(s, id));
	}

	/**
	 * Removes a single {@link QueryParserConfig} from the given
	 * {@link SearchConfig}. The removed config becomes an orphan
	 * and is deleted by {@code orphanRemoval} at flush time.
	 *
	 * @param id the ID of the {@link SearchConfig}
	 * @param queryParserConfigId the ID of the config to remove
	 * @return a {@link Uni} emitting the updated SearchConfig
	 *         and the removed config ID, or null if not found
	 */
	public Uni<Tuple2<SearchConfig, Long>> removeQueryParserConfig(long id, long queryParserConfigId) {
		return sessionFactory.withTransaction(s -> findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(searchConfig -> s.fetch(searchConfig.getQueryParserConfigs()).flatMap(
				queryParserConfigs -> {
					if (searchConfig.removeQueryParserConfig(queryParserConfigs, queryParserConfigId)) {
						return persist(s, searchConfig)
							.map(dt -> Tuple2.of(dt, queryParserConfigId));
					}
					return Uni.createFrom().nullItem();
				})));
	}

	/**
	 * Updates an existing {@link SearchConfig} by applying the new values from the provided DTO,
	 * and fully replaces its associated {@link QueryParserConfig}s if present.
	 *
	 * <p>If the {@code dto} is an instance of {@link SearchConfigWithQueryParsersDTO}, the method
	 * performs a full update of the main entity and all its related query parser configurations within
	 * a transactional context.</p>
	 *
	 * @param id the ID of the {@link SearchConfig} to update
	 * @param dto the data transfer object containing the updated fields and optional query parsers
	 * @return a {@link Uni} emitting the updated {@link SearchConfig}
	 */
	@Override
	public Uni<SearchConfig> update(long id, SearchConfigDTO dto) {
		if (dto instanceof SearchConfigWithQueryParsersDTO withQueryParsersDTO) {
			return sessionFactory.withTransaction(s -> findById(s, id)
				.call(searchConfig ->
					Mutiny.fetch(searchConfig.getQueryParserConfigs()))
				.flatMap(searchConfig -> {

					SearchConfig newStateSearchConfig =
						mapper.update(searchConfig, withQueryParsersDTO);

					// UniBuilder to prevent empty unis
					List<Uni<Void>> unis = new ArrayList<>();
					unis.add(Uni.createFrom().voidItem());

					List<QueryParserConfigDTO> queryParsers = withQueryParsersDTO.getQueryParsers();

					// removes old queryParserConfigs
					searchConfig.getQueryParserConfigs().clear();

					if (queryParsers != null) {

						// iterates all DTO's queryParser information
						unis.addAll(queryParsers.stream()
							.map(queryParserConfigDTO ->
								// creates a queryParser based on the DTO's information
								queryParserConfigService.create(s, queryParserConfigDTO)
									// binds the queryParser to the searchConfig
									.invoke(queryParserConfig ->
										searchConfig.addQueryParserConfig(
											newStateSearchConfig.getQueryParserConfigs(),
											queryParserConfig
										)
									)
									.replaceWithVoid()
							)
							.toList()
						);
					}

					return Uni.combine().all().unis(unis)
						.usingConcurrencyOf(1)
						.discardItems()
						.onFailure()
						.invoke(log::error)
						.flatMap(ignored -> s.merge(newStateSearchConfig));
				})
			);
		}
		return super.update(id, dto);
	}

}

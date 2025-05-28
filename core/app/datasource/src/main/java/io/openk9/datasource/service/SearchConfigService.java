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

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.common.util.StringUtils;
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
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.validation.constraints.NotNull;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.generic.Bodies;
import org.opensearch.client.opensearch.generic.Requests;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@ApplicationScoped
public class SearchConfigService extends BaseK9EntityService<SearchConfig, SearchConfigDTO> {

	private static final Logger log = Logger.getLogger(SearchConfigService.class);

	SearchConfigService(SearchConfigMapper mapper) {
		 this.mapper = mapper;
	}

	@Override
	public Uni<SearchConfig> create(SearchConfigDTO dto) {
		if (dto instanceof SearchConfigWithQueryParsersDTO withQueryParsersDTO) {
			SearchConfig transientSearchConfig = mapper.create(withQueryParsersDTO);

			return sessionFactory.withTransaction((s, tr) ->
				super.create(s, transientSearchConfig)
					.flatMap(searchConfig -> {
						List<Uni<Void>> unis = List.of(Uni.createFrom().voidItem());

						var queryParsers = withQueryParsersDTO.getQueryParsers();

						if (queryParsers != null) {
							// iterates all DTO's queryParser information
							unis = queryParsers.stream()
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
								.toList();
						}

						return Uni.combine().all().unis(unis)
							.usingConcurrencyOf(1)
							.discardItems()
							.onFailure()
							.invoke(log::error)
							.flatMap(ignored -> s.merge(searchConfig));
					})
			);
		}
		return super.create(dto);
	}

	@Override
	public Class<SearchConfig> getEntityClass() {
		return SearchConfig.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {QueryAnalysis_.NAME, QueryAnalysis_.DESCRIPTION};
	}

	public Uni<Connection<QueryParserConfig>> getQueryParserConfigs(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, SearchConfig_.QUERY_PARSER_CONFIGS, QueryParserConfig.class,
			queryParserConfigService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual);
	}

	public Uni<Connection<QueryParserConfig>> getQueryParserConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {
		return findJoinConnection(
			id, SearchConfig_.QUERY_PARSER_CONFIGS, QueryParserConfig.class,
			queryParserConfigService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
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

	public Uni<Set<QueryParserConfig>> getQueryParserConfig(SearchConfig searchConfig) {
		return sessionFactory.withTransaction(s -> s.fetch(searchConfig.getQueryParserConfigs()));
	}

	public Uni<Tuple2<SearchConfig, QueryParserConfig>> addQueryParserConfig(
		long id, QueryParserConfigDTO queryParserConfigDTO) {

		QueryParserConfig queryParserConfig =
			_queryParserConfigMapper.create(queryParserConfigDTO);

		return sessionFactory.withTransaction((s) -> findById(s, id)
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

					List<QueryParserConfigDTO> queryParsers = withQueryParsersDTO.getQueryParsers();
					if (queryParsers != null) {
						unis.add(removeAllQueryParserConfig(s, id).replaceWithVoid());

						// iterates all DTO's queryParser information
						unis.addAll(withQueryParsersDTO.getQueryParsers().stream()
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
		return super.patch(id, dto);
	}

	public Uni<SearchConfig> removeAllQueryParserConfig(Mutiny.Session s, long id) {
		return findById(s, id)
			.onItem()
			.ifNotNull()
			.transformToUni(searchConfig ->
				s.fetch(searchConfig.getQueryParserConfigs())
					.flatMap(queryParserConfigs -> {
					searchConfig.removeAllQueryParserConfig(queryParserConfigs);
					return persist(s, searchConfig)
						.map(dt -> dt);
					})
			);
	}

	public Uni<SearchConfig> removeAllQueryParserConfig(long id) {
		return sessionFactory.withTransaction((s) -> removeAllQueryParserConfig(s, id));
	}

	public Uni<Tuple2<SearchConfig, Long>> removeQueryParserConfig(long id, long queryParserConfigId) {
		return sessionFactory.withTransaction((s) -> findById(s, id)
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
					unis.add(removeAllQueryParserConfig(s, id).replaceWithVoid());

					if (queryParsers != null) {

						// iterates all DTO's queryParser information
						unis.addAll(withQueryParsersDTO.getQueryParsers().stream()
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

	@Inject
	OpenSearchClient openSearchClient;
	@Inject
	QueryParserConfigMapper _queryParserConfigMapper;
	@Inject
	QueryParserConfigService queryParserConfigService;

	public Uni<SearchPipelineResponseDTO> configureHybridSearch(
		long id, @NotNull HybridSearchPipelineDTO pipelineDTO) {

		return sessionFactory.withTransaction((s) -> findById(s, id))
			.flatMap(searchConfig -> Uni.createFrom()
				.completionStage(openSearchClient
					.generic()
					.executeAsync(Requests.builder()
						.method("PUT")
						.endpoint(
							"_search/pipeline/" + StringUtils.retainsAlnum(searchConfig.getName()))
						.json(getJsonBody(pipelineDTO)
						)
						.build()
					)
				)
			)
			.map(response -> new SearchPipelineResponseDTO(
				response.getStatus(),
				response.getBody().orElse(Bodies.json("{}")).bodyAsString(),
				response.getReason()
			));
	}

	protected static JsonObject getJsonBody(HybridSearchPipelineDTO pipelineDTO) {
		return Json.createObjectBuilder()
			.add("description", "Post processor for hybrid search")
			.add("phase_results_processors", Json.createArrayBuilder()
				.add(Json.createObjectBuilder()
					.add("normalization-processor", Json.createObjectBuilder()
						.add("normalization", Json.createObjectBuilder()
							.add(
								"technique",
								pipelineDTO.getNormalizationTechnique().getValue()
							)
						)
						.add("combination", Json.createObjectBuilder()
							.add(
								"technique",
								pipelineDTO.getCombinationTechnique().getValue()
							)
							.add("parameters", Json.createObjectBuilder()
								.add(
									"weights",
									Json.createArrayBuilder(
										pipelineDTO.getWeights()
									)

								)
							)
						)
					)
				)
			)
			.build();
	}

}

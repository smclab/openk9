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

import io.openk9.datasource.graphql.util.relay.Connection;
import io.openk9.datasource.mapper.SearchConfigMapper;
import io.openk9.datasource.model.QueryAnalysis_;
import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.SearchConfig_;
import io.openk9.datasource.model.dto.SearchConfigDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class SearchConfigService extends BaseK9EntityService<SearchConfig, SearchConfigDTO> {
	 SearchConfigService(SearchConfigMapper mapper) {
		 this.mapper = mapper;
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

	public Uni<Tuple2<SearchConfig, QueryParserConfig>> addQueryParserConfigToSearchConfig(long id, long queryParserConfigId) {
		return withTransaction((s, tr) -> findById(id)
				.onItem()
				.ifNotNull()
				.transformToUni(searchConfig -> queryParserConfigService.findById(queryParserConfigId)
						.onItem()
						.ifNotNull()
						.transformToUni(queryParserConfig ->
								Mutiny2.fetch(s, searchConfig.getQueryParserConfigs())
										.onItem()
										.ifNotNull()
										.transformToUni(queryParserConfigs -> {

											if (queryParserConfigs.add(queryParserConfig)) {

												searchConfig.setQueryParserConfigs(queryParserConfigs);

												return persist(searchConfig)
														.map(newSC -> Tuple2.of(newSC, queryParserConfig));
											}

											return Uni.createFrom().nullItem();

										})
						)
				));
	}

	public Uni<Tuple2<SearchConfig, QueryParserConfig>> removeQueryParserConfigFromSearchConfig(long id, long queryParserConfigId) {

		return withTransaction((s, tr) -> findById(id)
				.onItem()
				.ifNotNull()
				.transformToUni(searchConfig -> Mutiny2.fetch(s, searchConfig.getQueryParserConfigs())
						.onItem()
						.ifNotNull()
						.transformToUni(queryParserConfigs -> {

							if (searchConfig.removeQueryParserConfig(queryParserConfigs, queryParserConfigId)) {

								return persist(searchConfig)
										.map(newSC -> Tuple2.of(newSC, null));
							}

							return Uni.createFrom().nullItem();

						})));
	}

	@Inject
	QueryParserConfigService queryParserConfigService;

}

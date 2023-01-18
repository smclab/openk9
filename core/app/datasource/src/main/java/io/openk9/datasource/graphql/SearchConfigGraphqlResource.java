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

package io.openk9.datasource.graphql;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.FieldValidator;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.dto.QueryParserConfigDTO;
import io.openk9.datasource.model.dto.SearchConfigDTO;
import io.openk9.datasource.service.QueryParserConfigService;
import io.openk9.datasource.service.SearchConfigService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class SearchConfigGraphqlResource {

	@Query
	public Uni<Connection<SearchConfig>> getSearchConfigs(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return searchConfigService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<SearchConfig> getSearchConfig(@Id long id) {
		return searchConfigService.findById(id);
	}


	public Uni<Connection<QueryParserConfig>> queryParserConfigs(
		@Source SearchConfig searchConfig,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@DefaultValue("false") boolean notEqual) {

		return queryParserConfigs(
			searchConfig.getId(), after, before, first, last, searchText,
			sortByList, notEqual);
	}

	@Query
	public Uni<Connection<QueryParserConfig>> queryParserConfigs(
		@Id long searchConfigId,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@DefaultValue("false") boolean notEqual) {

		return searchConfigService.getQueryParserConfigs(
			searchConfigId, after, before, first, last, searchText,
			sortByList, notEqual);
	}

	@Query
	public Uni<QueryParserConfig> getQueryParserConfig(@Id long id) {
		return _queryParserConfigService.findById(id);
	}

	public Uni<Response<SearchConfig>> patchSearchConfig(@Id long id, SearchConfigDTO searchConfigDTO) {
		return searchConfigService.getValidator().patch(id, searchConfigDTO);
	}

	public Uni<Response<SearchConfig>> updateSearchConfig(@Id long id, SearchConfigDTO searchConfigDTO) {
		return searchConfigService.getValidator().update(id, searchConfigDTO);
	}

	public Uni<Response<SearchConfig>> createSearchConfig(SearchConfigDTO searchConfigDTO) {
		return searchConfigService.getValidator().create(searchConfigDTO);
	}

	@Mutation
	public Uni<Response<QueryParserConfig>> queryParserConfig(
		@Id long searchConfigId, @Id Long queryParserConfigId, QueryParserConfigDTO queryParserConfigDTO,
		@DefaultValue("false") boolean patch) {

		return Uni.createFrom().deferred(() -> {

			List<FieldValidator> validatorList =
				_queryParserConfigService.getValidator().validate(queryParserConfigDTO);

			if (validatorList.isEmpty()) {

				if (queryParserConfigId == null) {
					return searchConfigService.addQueryParserConfig(searchConfigId, queryParserConfigDTO)
						.map(e -> Response.of(e.right, null));
				} else {
					return (
						patch
							? _queryParserConfigService.patch(queryParserConfigId, queryParserConfigDTO)
							: _queryParserConfigService.update(queryParserConfigId, queryParserConfigDTO)
					).map(e -> Response.of(e, null));
				}

			}

			return Uni.createFrom().item(Response.of(null, validatorList));
		});

	}

	@Mutation
	public Uni<Tuple2<SearchConfig, Long>> removeQueryParserConfig(
		@Id long searchConfigId, @Id long queryParserConfigId) {
		return searchConfigService.removeQueryParserConfig(searchConfigId, queryParserConfigId);
	}


	@Mutation
	public Uni<Response<SearchConfig>> searchConfig(
		@Id Long id, SearchConfigDTO searchConfigDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createSearchConfig(searchConfigDTO);
		} else {
			return patch
				? patchSearchConfig(id, searchConfigDTO)
				: updateSearchConfig(id, searchConfigDTO);
		}

	}

	@Mutation
	public Uni<SearchConfig> deleteSearchConfig(@Id long searchConfigId) {
		return searchConfigService.deleteById(searchConfigId);
	}


	@Subscription
	public Multi<SearchConfig> searchConfigCreated() {
		return searchConfigService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<SearchConfig> searchConfigDeleted() {
		return searchConfigService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<SearchConfig> searchConfigUpdated() {
		return searchConfigService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	SearchConfigService searchConfigService;
	@Inject
	QueryParserConfigService _queryParserConfigService;

}
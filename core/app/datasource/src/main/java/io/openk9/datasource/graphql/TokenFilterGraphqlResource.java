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

import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.dto.base.TokenFilterDTO;
import io.openk9.datasource.service.TokenFilterService;
import io.openk9.datasource.service.util.K9EntityEvent;

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

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class TokenFilterGraphqlResource {

	@Query
	public Uni<Connection<TokenFilter>> getTokenFilters(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return _tokenFilterService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<TokenFilter> getTokenFilter(@Id long id) {
		return _tokenFilterService.findById(id);
	}

	public Uni<Response<TokenFilter>> patchTokenFilter(@Id long id, TokenFilterDTO tokenFilterDTO) {
		return _tokenFilterService.getValidator().patch(id, tokenFilterDTO);
	}

	public Uni<Response<TokenFilter>> updateTokenFilter(@Id long id, TokenFilterDTO tokenFilterDTO) {
		return _tokenFilterService.getValidator().update(id, tokenFilterDTO);
	}

	public Uni<Response<TokenFilter>> createTokenFilter(TokenFilterDTO tokenFilterDTO) {
		return _tokenFilterService.getValidator().create(tokenFilterDTO);
	}

	@Mutation
	public Uni<Response<TokenFilter>> tokenFilter(
		@Id Long id, TokenFilterDTO tokenFilterDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createTokenFilter(tokenFilterDTO);
		} else {
			return patch
				? patchTokenFilter(id, tokenFilterDTO)
				: updateTokenFilter(id, tokenFilterDTO);
		}

	}

	@Mutation
	public Uni<TokenFilter> deleteTokenFilter(@Id long tokenFilterId) {
		return _tokenFilterService.deleteById(tokenFilterId);
	}

	@Subscription
	public Multi<TokenFilter> tokenFilterCreated() {
		return _tokenFilterService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<TokenFilter> tokenFilterDeleted() {
		return _tokenFilterService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<TokenFilter> tokenFilterUpdated() {
		return _tokenFilterService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	TokenFilterService _tokenFilterService;
}

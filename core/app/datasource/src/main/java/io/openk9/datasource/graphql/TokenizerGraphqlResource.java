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

import io.openk9.common.graphql.SortBy;
import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.web.Response;
import io.openk9.datasource.model.Tokenizer;
import io.openk9.datasource.model.dto.base.TokenizerDTO;
import io.openk9.datasource.service.TokenizerService;
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
public class TokenizerGraphqlResource {

	@Query
	public Uni<Connection<Tokenizer>> getTokenizers(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return _tokenizerService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<Tokenizer> getTokenizer(@Id long id) {
		return _tokenizerService.findById(id);
	}

	public Uni<Response<Tokenizer>> patchTokenizer(@Id long id, TokenizerDTO tokenizerDTO) {
		return _tokenizerService.getValidator().patch(id, tokenizerDTO);
	}

	public Uni<Response<Tokenizer>> updateTokenizer(@Id long id, TokenizerDTO tokenizerDTO) {
		return _tokenizerService.getValidator().update(id, tokenizerDTO);
	}

	public Uni<Response<Tokenizer>> createTokenizer(TokenizerDTO tokenizerDTO) {
		return _tokenizerService.getValidator().create(tokenizerDTO);
	}

	@Mutation
	public Uni<Response<Tokenizer>> tokenizer(
		@Id Long id, TokenizerDTO tokenizerDTO ,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createTokenizer(tokenizerDTO);
		} else {
			return patch
				? patchTokenizer(id, tokenizerDTO)
				: updateTokenizer(id, tokenizerDTO);
		}

	}

	@Mutation
	public Uni<Tokenizer> deleteTokenizer(@Id long tokenizerId) {
		return _tokenizerService.deleteById(tokenizerId);
	}

	@Subscription
	public Multi<Tokenizer> tokenizerCreated() {
		return _tokenizerService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Tokenizer> tokenizerDeleted() {
		return _tokenizerService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Tokenizer> tokenizerUpdated() {
		return _tokenizerService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}


	@Inject
	TokenizerService _tokenizerService;
}

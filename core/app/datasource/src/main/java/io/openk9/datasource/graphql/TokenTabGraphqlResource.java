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

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.graphql.dto.TokenTabWithDocTypeFieldDTO;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.dto.TokenTabDTO;
import io.openk9.datasource.service.TokenTabService;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class TokenTabGraphqlResource {

	@Mutation
	public Uni<TokenTab> addExtraParam(@Id long id, String key, String value) {
		return tokenTabService.addExtraParam(id, key, value);
	}

	@Mutation
	public Uni<Tuple2<TokenTab, DocTypeField>> bindDocTypeFieldToTokenTab(
		@Id long tokenTabId, @Id long docTypeFieldId) {
		return tokenTabService.bindDocTypeFieldToTokenTab(tokenTabId, docTypeFieldId);
	}

	public Uni<Response<TokenTab>> createTokenTab(TokenTabDTO tokenTabDTO) {
		return tokenTabService.getValidator().create(tokenTabDTO);
	}

	@Mutation
	public Uni<TokenTab> deleteTokenTab(@Id long tokenTabId) {
		return tokenTabService.deleteById(tokenTabId);
	}

	public Uni<DocTypeField> docTypeField(@Source TokenTab tokenTab) {
		return tokenTabService.getDocTypeField(tokenTab);
	}

	public Uni<Connection<DocTypeField>> docTypeFieldsNotInTokenTab(
		@Source TokenTab tokenTab,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {

		return tokenTabService.getDocTypeFieldsNotInTokenTab(
			tokenTab.getId(), after, before, first, last, searchText, sortByList);
	}

	public Uni<Set<TokenTab.ExtraParam>> extraParams(@Source TokenTab tokenTab) {
		return tokenTabService.getExtraParams(tokenTab);
	}

	@Query
	public Uni<Connection<DocTypeField>> getDocTypeFieldsNotInTokenTab(
		@Id long tokenTabId,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {
		return tokenTabService.getDocTypeFieldsNotInTokenTab(
			tokenTabId, after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<TokenTab> getTokenTab(@Id long id) {
		return tokenTabService.findById(id);
	}

	@Query
	public Uni<Connection<TokenTab>> getTotalTokenTabs(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return tokenTabService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<List<Tab>> getUnboundTabByTokenTab(long tokenTabId) {
		return tokenTabService.getUnboundTabByTokenTab(tokenTabId);
	}

	@Mutation
	public Uni<TokenTab> removeExtraParam(@Id int id, String key) {
		return tokenTabService.removeExtraParam(id, key);
	}

	public Uni<Response<TokenTab>> tokenTab(@Id long id, TokenTabDTO tokenTabDTO) {
		return tokenTabService.getValidator().patch(id, tokenTabDTO);
	}

	@Mutation
	public Uni<Response<TokenTab>> tokenTab(
		@Id Long id, TokenTabDTO tokenTabDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createTokenTab(tokenTabDTO);
		} else {
			return patch
				? tokenTab(id, tokenTabDTO)
				: updateTokenTab(id, tokenTabDTO);
		}

	}

	@Mutation
	@Description("API to create, patch or update tokenTab with the possibility to link a docTypeField ")
	public Uni<Response<TokenTab>> tokenTabWithDocTypeField(
		@Id Long id, TokenTabWithDocTypeFieldDTO tokenTabWithDocTypeFieldDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createTokenTab(tokenTabWithDocTypeFieldDTO);
		} else {
			return patch
				? tokenTab(id, tokenTabWithDocTypeFieldDTO)
				: updateTokenTab(id, tokenTabWithDocTypeFieldDTO);
		}

	}

	@Mutation
	public Uni<Tuple2<TokenTab, DocTypeField>> unbindDocTypeFieldFromTokenTab(
		@Id long id, @Id long docTypeFieldId) {
		return tokenTabService.unbindDocTypeFieldFromTokenTab(id, docTypeFieldId);
	}

	public Uni<Response<TokenTab>> updateTokenTab(@Id long id, TokenTabDTO tokenTabDTO) {
		return tokenTabService.getValidator().update(id, tokenTabDTO);
	}

	@Inject
	TokenTabService tokenTabService;
}

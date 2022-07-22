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

import io.openk9.datasource.graphql.util.relay.Connection;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.dto.SuggestionCategoryDTO;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.SuggestionCategoryService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.validation.Response;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class SuggestionCategoryGraphqlResource {

	@Query
	public Uni<Connection<SuggestionCategory>> getSuggestionCategories(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return suggestionCategoryService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	public Uni<Connection<DocTypeField>> docTypeFields(
		@Source SuggestionCategory suggestionCategory,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return suggestionCategoryService.getDocTypeFieldsConnection(
			suggestionCategory.getId(), after, before, first, last, searchText,
			sortByList);
	}

	@Query
	public Uni<SuggestionCategory> getSuggestionCategory(long id) {
		return suggestionCategoryService.findById(id);
	}

	public Uni<Response<SuggestionCategory>> patchSuggestionCategory(long id, SuggestionCategoryDTO suggestionCategoryDTO) {
		return suggestionCategoryService.getValidator().patch(id, suggestionCategoryDTO);
	}

	public Uni<Response<SuggestionCategory>> updateSuggestionCategory(long id, SuggestionCategoryDTO suggestionCategoryDTO) {
		return suggestionCategoryService.getValidator().update(id, suggestionCategoryDTO);
	}

	public Uni<Response<SuggestionCategory>> createSuggestionCategory(SuggestionCategoryDTO suggestionCategoryDTO) {
		return suggestionCategoryService.getValidator().create(suggestionCategoryDTO);
	}

	@Mutation
	public Uni<Response<SuggestionCategory>> suggestionCategory(
		Long id, SuggestionCategoryDTO suggestionCategoryDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createSuggestionCategory(suggestionCategoryDTO);
		} else {
			return patch
				? patchSuggestionCategory(id, suggestionCategoryDTO)
				: updateSuggestionCategory(id, suggestionCategoryDTO);
		}

	}

	@Mutation
	public Uni<SuggestionCategory> deleteSuggestionCategory(long suggestionCategoryId) {
		return suggestionCategoryService.deleteById(suggestionCategoryId);
	}

	@Mutation
	public Uni<Tuple2<SuggestionCategory, DocTypeField>> addDocTypeFieldToSuggestionCategory(long suggestionCategoryId, long docTypeFieldId) {
		return suggestionCategoryService.addDocTypeField(suggestionCategoryId, docTypeFieldId);
	}

	@Mutation
	public Uni<Tuple2<SuggestionCategory, DocTypeField>> removeDocTypeFieldToSuggestionCategory(long suggestionCategoryId, long docTypeFieldId) {
		return suggestionCategoryService.removeDocTypeField(suggestionCategoryId, docTypeFieldId);
	}

	@Subscription
	public Multi<SuggestionCategory> suggestionCategoryCreated() {
		return suggestionCategoryService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<SuggestionCategory> suggestionCategoryDeleted() {
		return suggestionCategoryService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<SuggestionCategory> suggestionCategoryUpdated() {
		return suggestionCategoryService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	SuggestionCategoryService suggestionCategoryService;

}
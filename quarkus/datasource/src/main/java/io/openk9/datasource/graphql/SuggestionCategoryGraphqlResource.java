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

import io.openk9.datasource.graphql.util.Response;
import io.openk9.datasource.graphql.util.SortType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.dto.SuggestionCategoryDTO;
import io.openk9.datasource.resource.util.K9Column;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.SuggestionCategoryService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class SuggestionCategoryGraphqlResource {

	@Query
	public Uni<Page<SuggestionCategory>> getSuggestionCategories(
		@Name("limit") @DefaultValue("20") int limit,
		@Name("offset") @DefaultValue("0") int offset,
		@Name("sortBy") @DefaultValue("createDate") K9Column sortBy,
		@Name("sortType") @DefaultValue("ASC") SortType sortType) {
		return suggestionCategoryService.findAllPaginated(
			limit, offset, sortBy.name(), sortType);
	}

	public Uni<Page<DocTypeField>> docTypeFields(
		@Source SuggestionCategory suggestionCategory,
		@Name("limit") @DefaultValue("20") int limit,
		@Name("offset") @DefaultValue("0") int offset,
		@Name("sortBy") @DefaultValue("createDate") K9Column sortBy,
		@Name("sortType") @DefaultValue("ASC") SortType sortType) {
		return suggestionCategoryService.getDocTypeFields(
			suggestionCategory.getId(), Pageable.of(limit, offset, sortBy, sortType));
	}

	@Query
	public Uni<SuggestionCategory> getSuggestionCategory(long id) {
		return suggestionCategoryService.findById(id);
	}

	@Mutation
	public Uni<SuggestionCategory> patchSuggestionCategory(long id, SuggestionCategoryDTO suggestionCategoryDTO) {
		return suggestionCategoryService.patch(id, suggestionCategoryDTO);
	}

	@Mutation
	public Uni<SuggestionCategory> updateSuggestionCategory(long id, SuggestionCategoryDTO suggestionCategoryDTO) {
		return suggestionCategoryService.update(id, suggestionCategoryDTO);
	}

	@Mutation
	public Uni<SuggestionCategory> createSuggestionCategory(SuggestionCategoryDTO suggestionCategoryDTO) {
		return suggestionCategoryService.persist(suggestionCategoryDTO);
	}

	@Mutation
	public Uni<SuggestionCategory> deleteSuggestionCategory(long suggestionCategoryId) {
		return suggestionCategoryService.deleteById(suggestionCategoryId);
	}

	@Mutation
	public Uni<Response> addDocTypeFieldToSuggestionCategory(long suggestionCategoryId, long docTypeFieldId) {
		return suggestionCategoryService.addDocTypeField(suggestionCategoryId, docTypeFieldId)
			.replaceWith(() -> Response.of("DocTypeField added to SuggestionCategory"));
	}

	@Mutation
	public Uni<Response> removeDocTypeFieldToSuggestionCategory(long suggestionCategoryId, long docTypeFieldId) {
		return suggestionCategoryService.removeDocTypeField(suggestionCategoryId, docTypeFieldId)
			.replaceWith(() -> Response.of("DocTypeField removed from SuggestionCategory"));
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
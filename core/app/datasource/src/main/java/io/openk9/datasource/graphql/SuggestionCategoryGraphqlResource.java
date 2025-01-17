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
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.graphql.dto.SuggestionCategoryWithDocTypeFieldDTO;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.dto.SuggestionCategoryDTO;
import io.openk9.datasource.model.dto.TranslationDTO;
import io.openk9.datasource.service.SuggestionCategoryService;
import io.openk9.datasource.service.TranslationService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

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

	public Uni<Bucket> bucket(
		@Source SuggestionCategory suggestionCategory) {

		return suggestionCategoryService.getBucket(
			suggestionCategory.getId());
	}

	public Uni<DocTypeField> docTypeField(
			@Source SuggestionCategory suggestionCategory) {

		return suggestionCategoryService.getDocTypeField(
			suggestionCategory.getId());
	}

	@Query
	public Uni<SuggestionCategory> getSuggestionCategory(@Id long id) {
		return suggestionCategoryService.findById(id);
	}

	public Uni<Set<TranslationDTO>> getTranslations(
		@Source SuggestionCategory suggestionCategory) {

		return translationService
			.getTranslationDTOs(SuggestionCategory.class, suggestionCategory.getId());
	}


	public Uni<Response<SuggestionCategory>> patchSuggestionCategory(@Id long id, SuggestionCategoryDTO suggestionCategoryDTO) {
		return suggestionCategoryService.getValidator().patch(id, suggestionCategoryDTO);
	}

	public Uni<Response<SuggestionCategory>> updateSuggestionCategory(@Id long id, SuggestionCategoryDTO suggestionCategoryDTO) {
		return suggestionCategoryService.getValidator().update(id, suggestionCategoryDTO);
	}

	public Uni<Response<SuggestionCategory>> createSuggestionCategory(SuggestionCategoryDTO suggestionCategoryDTO) {
		return suggestionCategoryService.getValidator().create(suggestionCategoryDTO);
	}

	@Mutation
	public Uni<Response<SuggestionCategory>> suggestionCategory(
		@Id Long id, SuggestionCategoryDTO suggestionCategoryDTO,
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
	public Uni<Response<SuggestionCategory>> suggestionCategoryWithDocTypeField(
		@Id Long id, SuggestionCategoryWithDocTypeFieldDTO suggestionCategoryWithDocTypeFieldDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createSuggestionCategory(suggestionCategoryWithDocTypeFieldDTO);
		} else {
			return patch
				? patchSuggestionCategory(id, suggestionCategoryWithDocTypeFieldDTO)
				: updateSuggestionCategory(id, suggestionCategoryWithDocTypeFieldDTO);
		}

	}

	@Mutation
	public Uni<SuggestionCategory> deleteSuggestionCategory(@Id long suggestionCategoryId) {
		return suggestionCategoryService.deleteById(suggestionCategoryId);
	}

	@Mutation
	public Uni<Tuple2<SuggestionCategory, DocTypeField>> addDocTypeFieldToSuggestionCategory(@Id long suggestionCategoryId, @Id long docTypeFieldId) {
		return suggestionCategoryService.addDocTypeField(suggestionCategoryId, docTypeFieldId);
	}

	/**
	 * This GraphQL mutation has been deprecated and replaced by the {@code unbindDocTypeFieldFromSuggestionCategory} method,
	 * which no longer requires the {@code docTypeFieldId} parameter.
	 *
	 * <p>
	 * Deprecation introduced in version 3.0.0. While there are no significant impacts from using this method,
	 * it is recommended to transition to the new method for improved functionality and future compatibility.
	 * </p>
	 *
	 * @deprecated Use {@link #unbindDocTypeFieldFromSuggestionCategory(long suggestionCategoryId)} instead.
	 */
	@Mutation
	@Description("This mutation is deprecated. Use `unbindDocTypeFieldFromSuggestionCategory` instead. " +
		"Deprecation introduced in version 3.0.0. No significant impacts from usage, " +
		"but transitioning to the new method is recommended.")
	@Deprecated
	public Uni<Tuple2<SuggestionCategory, DocTypeField>> removeDocTypeFieldFromSuggestionCategory(
			@Id long suggestionCategoryId, @Id long docTypeFieldId) {
		return suggestionCategoryService.unsetDocTypeField(suggestionCategoryId)
			.map(sc -> Tuple2.of(sc, null));
	}

	/**
	 * Unbinds a {@link DocTypeField} from a {@link SuggestionCategory}.
	 *
	 * <p>
	 * This method replaces the deprecated {@code removeDocTypeFieldFromSuggestionCategory(long suggestionCategoryId, long docTypeFieldId)}.
	 * The new implementation improves usability by eliminating the need to provide the {@code docTypeFieldId}.
	 * </p>
	 *
	 * @param suggestionCategoryId The ID of the {@link SuggestionCategory} from which to unbind the {@link DocTypeField}.
	 * @return A {@link Uni} containing the updated {@link SuggestionCategory} and the unbound {@link DocTypeField}.
	 *
	 * @see #removeDocTypeFieldFromSuggestionCategory(long, long)
	 */
	@Mutation
	@Description("This mutation replaces `removeDocTypeFieldFromSuggestionCategory`. It does not require the `docTypeFieldId` parameter " +
		"and provides a more efficient implementation.")
	public Uni<SuggestionCategory> unbindDocTypeFieldFromSuggestionCategory(
			@Id long suggestionCategoryId) {
		return suggestionCategoryService.unsetDocTypeField(suggestionCategoryId);
	}

	@Mutation
	public Uni<SuggestionCategory> setMultiSelect(
		@Id @Name("suggestionCategoryId") long suggestionCategoryId, boolean multiSelect) {
		return suggestionCategoryService.setMultiSelect(suggestionCategoryId, multiSelect);
	}

	@Mutation
	public Uni<Tuple2<String, String>> addSuggestionCategoryTranslation(
		@Id @Name("suggestionCategoryId") long suggestionCategoryId,
		String language, String key, String value) {

		return translationService
			.addTranslation(SuggestionCategory.class, suggestionCategoryId, language, key, value)
			.map((__) -> Tuple2.of("ok", null));
	}

	@Mutation
	public Uni<Tuple2<String, String>> deleteSuggestionCategoryTranslation(
		@Id @Name("suggestionCategoryId") long suggestionCategoryId,
		String language, String key) {

		return translationService
			.deleteTranslation(SuggestionCategory.class, suggestionCategoryId, language, key)
			.map((__) -> Tuple2.of("ok", null));
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

	@Inject
	TranslationService translationService;
}
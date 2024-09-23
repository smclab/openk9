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
import io.openk9.datasource.graphql.dto.SuggestionCategoryWithDocTypeFieldDTO;
import io.openk9.datasource.mapper.SuggestionCategoryMapper;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.SuggestionCategory_;
import io.openk9.datasource.model.dto.SuggestionCategoryDTO;
import io.openk9.datasource.model.dto.TranslationDTO;
import io.openk9.datasource.model.dto.TranslationKeyDTO;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Set;
import java.util.stream.Collectors;

;

@ApplicationScoped
public class SuggestionCategoryService extends
	BaseK9EntityService<SuggestionCategory, SuggestionCategoryDTO> {
	 SuggestionCategoryService(SuggestionCategoryMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<SuggestionCategory> create(SuggestionCategoryDTO suggestionDTO){

		if (suggestionDTO instanceof
			SuggestionCategoryWithDocTypeFieldDTO withDocTypeFieldDTO) {

			var transientSuggestion =
				mapper.create(withDocTypeFieldDTO);

			return sessionFactory.withTransaction(
				(s, transaction) -> super.create(s, transientSuggestion)
					.flatMap(suggestion -> {
						var docTypeFields =
							withDocTypeFieldDTO.getDocTypeFieldIds().stream()
								.map(docTypeFieldId ->
									s.getReference(DocTypeField.class, docTypeFieldId))
								.collect(Collectors.toSet());

						suggestion.setDocTypeFields(docTypeFields);

						return s.persist(suggestion)
							.flatMap(__ -> s.merge(suggestion));
					})
			);
		}

		return super.create(suggestionDTO);
	}

	public Uni<SuggestionCategory> patch(long suggestionId,
		SuggestionCategoryDTO suggestionDTO) {

		if (suggestionDTO instanceof
			SuggestionCategoryWithDocTypeFieldDTO withDocTypeFieldDTO) {

			return sessionFactory.withTransaction(
				(s, transaction) -> findById(s, suggestionId)
					.call(suggestion -> Mutiny.fetch(suggestion.getDocTypeFields()))
					.flatMap(suggestion -> {
						var newStateSuggestion =
							mapper.patch(suggestion, withDocTypeFieldDTO);
						var docTypeFieldIds = withDocTypeFieldDTO.getDocTypeFieldIds();

						if (docTypeFieldIds != null) {
							var docTypeFields = docTypeFieldIds.stream()
								.map(docTypeFieldId ->
									s.getReference(DocTypeField.class, docTypeFieldId))
								.collect(Collectors.toSet());

							newStateSuggestion.getDocTypeFields().clear();
							newStateSuggestion.setDocTypeFields(docTypeFields);
						}

						return s.merge(newStateSuggestion)
							.map(__ -> newStateSuggestion);
					}));
		}

		return super.patch(suggestionId, suggestionDTO);
	}

	public Uni<SuggestionCategory> update(long suggestionId,
		SuggestionCategoryDTO suggestionDTO) {

		if (suggestionDTO instanceof
			SuggestionCategoryWithDocTypeFieldDTO withDocTypeFieldDTO) {

			return sessionFactory.withTransaction(
				(s, transaction) -> findById(s, suggestionId)
					.call(suggestion -> Mutiny.fetch(suggestion.getDocTypeFields()))
					.flatMap(suggestion -> {
						var newStateSuggestion =
							mapper.update(suggestion, withDocTypeFieldDTO);
						var docTypeFieldIds = withDocTypeFieldDTO.getDocTypeFieldIds();

						newStateSuggestion.getDocTypeFields().clear();

						if (docTypeFieldIds != null) {
							var docTypeFields = docTypeFieldIds.stream()
								.map(docTypeFieldId ->
									s.getReference(DocTypeField.class, docTypeFieldId))
								.collect(Collectors.toSet());

							newStateSuggestion.setDocTypeFields(docTypeFields);
						}

						return s.merge(newStateSuggestion)
							.map(__ -> newStateSuggestion);
					}));
		}

		return super.update(suggestionId, suggestionDTO);
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {
			SuggestionCategory_.NAME,
			SuggestionCategory_.DESCRIPTION,
			SuggestionCategory_.PRIORITY
		};
	}

	public Uni<Connection<DocTypeField>> getDocTypeFieldsConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		Uni<Connection<DocTypeField>> joinConnection =
			findJoinConnection(
				id, SuggestionCategory_.DOC_TYPE_FIELDS, DocTypeField.class,
				docTypeFieldService.getSearchFields(),
				after, before, first, last, searchText, sortByList, notEqual
			);

		if (notEqual) {
			return joinConnection
				.map(connection -> connection.filter(dtf -> dtf.isKeyword() || dtf.isI18N()));
		}

		return joinConnection;

	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long suggestionCategoryId, Pageable pageable) {
		 return getDocTypeFields(
			suggestionCategoryId, pageable, Filter.DEFAULT);
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long suggestionCategoryId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[] { suggestionCategoryId },
			SuggestionCategory_.DOC_TYPE_FIELDS, DocTypeField.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(), searchText
		);
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long suggestionCategoryId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[] { suggestionCategoryId },
			SuggestionCategory_.DOC_TYPE_FIELDS, DocTypeField.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(), filter
		);
	}

	public Uni<SuggestionCategory> setMultiSelect(long suggestionCategoryId, boolean multiSelect) {
		return sessionFactory.withTransaction(s -> findById(s, suggestionCategoryId)
			.onItem()
			.ifNotNull()
			.transformToUni(suggestionCategory -> {
				suggestionCategory.setMultiSelect(multiSelect);
				return persist(s, suggestionCategory);
			})
		);
	}

	public Uni<Tuple2<SuggestionCategory, DocTypeField>> addDocTypeField(
		long suggestionCategoryId, long docTypeFieldId) {
		return sessionFactory.withTransaction((s) -> findById(s, suggestionCategoryId)
			.onItem()
			.ifNotNull()
			.transformToUni(suggestionCategory -> docTypeFieldService.findById(s, docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transform(docTypeField -> (docTypeField.isKeyword() || docTypeField.isI18N())
					? docTypeField
					: null
				)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> s.fetch(suggestionCategory.getDocTypeFields()).flatMap(docTypeFields ->{
					if (docTypeFields.add(docTypeField)) {
						suggestionCategory.setDocTypeFields(docTypeFields);
						return persist(s, suggestionCategory).map(sc -> Tuple2.of(sc, docTypeField));
					}
					return Uni.createFrom().nullItem();

				}))));
	}

	public Uni<Tuple2<SuggestionCategory, DocTypeField>> removeDocTypeField(
		long suggestionCategoryId, long docTypeFieldId) {
		return sessionFactory.withTransaction((s) -> findById(s, suggestionCategoryId)
			.onItem()
			.ifNotNull()
			.transformToUni(suggestionCategory -> docTypeFieldService.findById(s, docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> s.fetch(suggestionCategory.getDocTypeFields()).flatMap(docTypeFields ->{
					if (docTypeFields.remove(docTypeField)) {
						suggestionCategory.setDocTypeFields(docTypeFields);
						return persist(s, suggestionCategory).map(sc -> Tuple2.of(sc, docTypeField));
					}
					return Uni.createFrom().nullItem();

				}))));
	}

	public Uni<Void> addTranslation(Long id, TranslationDTO dto) {
		return translationService.addTranslation(
			SuggestionCategory.class, id, dto.getLanguage(), dto.getKey(), dto.getValue());
	}

	public Uni<Void> deleteTranslation(Long id, TranslationKeyDTO dto) {
		return translationService.deleteTranslation(
			SuggestionCategory.class, id, dto.getLanguage(), dto.getKey());
	}

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	TranslationService translationService;

	@Override
	public Class<SuggestionCategory> getEntityClass() {
		return SuggestionCategory.class;
	}

}

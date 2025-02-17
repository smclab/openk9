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

import java.util.List;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.datasource.graphql.dto.SuggestionCategoryWithDocTypeFieldDTO;
import io.openk9.datasource.mapper.SuggestionCategoryMapper;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
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
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class SuggestionCategoryService extends
	BaseK9EntityService<SuggestionCategory, SuggestionCategoryDTO> {
	@Inject
	DocTypeFieldService docTypeFieldService;
	@Inject
	TranslationService translationService;

	 SuggestionCategoryService(SuggestionCategoryMapper mapper) {
		 this.mapper = mapper;
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
				.transformToUni(docTypeField -> {
					suggestionCategory.setDocTypeField(docTypeField);
					return persist(s, suggestionCategory).map(sc -> Tuple2.of(sc, docTypeField));
				})));
	}

	public Uni<Void> addTranslation(Long id, TranslationDTO dto) {
		return translationService.addTranslation(
			SuggestionCategory.class, id, dto.getLanguage(), dto.getKey(), dto.getValue());
	}

	public Uni<SuggestionCategory> create(SuggestionCategoryDTO suggestionDTO){

		if (suggestionDTO instanceof
			SuggestionCategoryWithDocTypeFieldDTO withDocTypeFieldDTO) {

			var transientSuggestion =
				mapper.create(withDocTypeFieldDTO);

			return sessionFactory.withTransaction(
				(s, transaction) -> super.create(s, transientSuggestion)
					.flatMap(suggestion -> docTypeFieldService
						.findById(withDocTypeFieldDTO.getDocTypeFieldId())
						.flatMap(docTypeField -> {
							suggestion.setDocTypeField(docTypeField);
							return s.merge(suggestion);
						})
					)
			);
		}

		return super.create(suggestionDTO);
	}

	public Uni<Void> deleteTranslation(Long id, TranslationKeyDTO dto) {
		return translationService.deleteTranslation(
			SuggestionCategory.class, id, dto.getLanguage(), dto.getKey());
	}

	/**
	 * Retrieves a list of DocTypeField entities that are unbound to the given SuggestionCategory ID
	 * and have a FieldType of either KEYWORD or I18N.
	 *
	 * @param suggestionCategoryId The ID of the SuggestionCategory to exclude associated DocTypeFields.
	 * @return A Uni containing a list of DocTypeField objects that meet the filtering criteria.
	 */
	public Uni<List<DocTypeField>> findUnboundDocTypeFieldsBySuggestionCategory(
			long suggestionCategoryId) {
		return sessionFactory.withTransaction(s -> {
			String queryString = "SELECT dtf FROM DocTypeField dtf " +
				"WHERE NOT EXISTS ( " +
				"SELECT 1 " +
				"FROM SuggestionCategory sc " +
				"WHERE sc.docTypeField.id = dtf.id " +
				"AND sc.id = (:suggestionCategoryId)) " +
				"AND (dtf.fieldType = (:keyword) OR dtf.fieldType = (:i18n))";

			return s.createQuery(queryString, DocTypeField.class)
				.setParameter("suggestionCategoryId", suggestionCategoryId)
				.setParameter("keyword", FieldType.KEYWORD)
				.setParameter("i18n", FieldType.I18N)
				.getResultList();
		});
	}

	public Uni<Set<Bucket>> getBuckets(long suggestionCategoryId) {
		return sessionFactory.withTransaction(s ->
			findById(s, suggestionCategoryId)
				.flatMap(suggestionCategory ->
					s.fetch(suggestionCategory.getBuckets()))
		);
	}

	public Uni<DocTypeField> getDocTypeField(long suggestionCategoryId) {
		return sessionFactory.withTransaction(s ->
			findById(s, suggestionCategoryId)
				.flatMap(suggestionCategory ->
					s.fetch(suggestionCategory.getDocTypeField()))
		);
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
			SuggestionCategory_.DOC_TYPE_FIELD, DocTypeField.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(), searchText
		);
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long suggestionCategoryId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[] { suggestionCategoryId },
			SuggestionCategory_.DOC_TYPE_FIELD, DocTypeField.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(), filter
		);
	}

	@Override
	public Class<SuggestionCategory> getEntityClass() {
		return SuggestionCategory.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {
			SuggestionCategory_.NAME,
			SuggestionCategory_.DESCRIPTION,
			SuggestionCategory_.PRIORITY
		};
	}

	public Uni<SuggestionCategory> patch(long suggestionId,
		SuggestionCategoryDTO suggestionDTO) {

		if (suggestionDTO instanceof
			SuggestionCategoryWithDocTypeFieldDTO withDocTypeFieldDTO) {

			return sessionFactory.withTransaction(
				(s, transaction) -> findById(s, suggestionId)
					.call(suggestion -> Mutiny.fetch(suggestion.getDocTypeField()))
					.flatMap(suggestion -> {
						var newStateSuggestion =
							mapper.patch(suggestion, withDocTypeFieldDTO);

						var docTypeFieldId = withDocTypeFieldDTO.getDocTypeFieldId();

						if (docTypeFieldId != null) {

							return docTypeFieldService.findById(s, docTypeFieldId)
								.flatMap(docTypeField -> {
									newStateSuggestion.setDocTypeField(docTypeField);
									return s.merge(newStateSuggestion);
								});
						}

						return s.merge(newStateSuggestion);

					}));
		}

		return super.patch(suggestionId, suggestionDTO);
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

	public Uni<SuggestionCategory> unsetDocTypeField(
		long suggestionCategoryId) {
		return sessionFactory.withTransaction((s) -> findById(s, suggestionCategoryId)
			.onItem()
			.ifNotNull()
			.transformToUni(suggestionCategory -> {
				suggestionCategory.setDocTypeField(null);
				return persist(s, suggestionCategory);
			}));
	}

	public Uni<SuggestionCategory> update(long suggestionId,
		SuggestionCategoryDTO suggestionDTO) {

		if (suggestionDTO instanceof
			SuggestionCategoryWithDocTypeFieldDTO withDocTypeFieldDTO) {

			return sessionFactory.withTransaction(
				(s, transaction) -> findById(s, suggestionId)
					.call(suggestion -> Mutiny.fetch(suggestion.getDocTypeField()))
					.flatMap(suggestion -> {
						var newStateSuggestion =
							mapper.update(suggestion, withDocTypeFieldDTO);
						var docTypeFieldId = withDocTypeFieldDTO.getDocTypeFieldId();

						newStateSuggestion.setDocTypeField(null);

						if (docTypeFieldId != null) {
							return docTypeFieldService.findById(s, docTypeFieldId)
								.flatMap(docTypeField -> {
									newStateSuggestion.setDocTypeField(docTypeField);
									return s.merge(newStateSuggestion);
								});
						}

						return s.merge(newStateSuggestion);

					}));
		}

		return super.update(suggestionId, suggestionDTO);
	}

}

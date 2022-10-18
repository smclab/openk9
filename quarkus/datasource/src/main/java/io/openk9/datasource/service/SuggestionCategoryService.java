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
import io.openk9.datasource.mapper.SuggestionCategoryMapper;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.SuggestionCategory_;
import io.openk9.datasource.model.dto.SuggestionCategoryDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class SuggestionCategoryService extends
	BaseK9EntityService<SuggestionCategory, SuggestionCategoryDTO> {
	 SuggestionCategoryService(SuggestionCategoryMapper mapper) {
		 this.mapper = mapper;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {SuggestionCategory_.NAME, SuggestionCategory_.DESCRIPTION,SuggestionCategory_.PRIORITY};
	}

	public Uni<Connection<DocTypeField>> getDocTypeFieldsConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, SuggestionCategory_.DOC_TYPE_FIELDS, DocTypeField.class,
			docTypeFieldService.getSearchFields(),
			after, before, first, last, searchText, sortByList, notEqual
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

	public Uni<Tuple2<SuggestionCategory, DocTypeField>> addDocTypeField(
		long suggestionCategoryId, long docTypeFieldId) {
		return withTransaction((s) -> findById(suggestionCategoryId)
			.onItem()
			.ifNotNull()
			.transformToUni(suggestionCategory -> docTypeFieldService.findById(docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> Mutiny2.fetch(s, suggestionCategory.getDocTypeFields()).flatMap(docTypeFields ->{
					if (docTypeFields.add(docTypeField)) {
						suggestionCategory.setDocTypeFields(docTypeFields);
						return persist(suggestionCategory).map(sc -> Tuple2.of(sc, docTypeField));
					}
					return Uni.createFrom().nullItem();

				}))));
	}

	public Uni<Tuple2<SuggestionCategory, DocTypeField>> removeDocTypeField(
		long suggestionCategoryId, long docTypeFieldId) {
		return withTransaction((s) -> findById(suggestionCategoryId)
			.onItem()
			.ifNotNull()
			.transformToUni(suggestionCategory -> docTypeFieldService.findById(docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> Mutiny2.fetch(s, suggestionCategory.getDocTypeFields()).flatMap(docTypeFields ->{
					if (docTypeFields.remove(docTypeField)) {
						suggestionCategory.setDocTypeFields(docTypeFields);
						return persist(suggestionCategory).map(sc -> Tuple2.of(sc, docTypeField));
					}
					return Uni.createFrom().nullItem();

				}))));
	}

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Override
	public Class<SuggestionCategory> getEntityClass() {
		return SuggestionCategory.class;
	}

}
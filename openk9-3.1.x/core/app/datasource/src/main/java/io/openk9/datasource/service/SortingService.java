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

import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.graphql.SortBy;
import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.datasource.mapper.SortingMapper;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.model.Sorting_;
import io.openk9.datasource.model.dto.base.SortingDTO;
import io.openk9.datasource.model.dto.base.TranslationDTO;
import io.openk9.datasource.model.dto.base.TranslationKeyDTO;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;

;

@ApplicationScoped
public class SortingService extends BaseK9EntityService<Sorting, SortingDTO> {
	SortingService(SortingMapper mapper) {this.mapper = mapper;}

	@Override
	public Class<Sorting> getEntityClass(){
		return Sorting.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {Sorting_.NAME, Sorting_.DESCRIPTION};
	}

	public Uni<DocTypeField> getDocTypeField(Sorting sorting) {
		return sessionFactory.withTransaction(s -> s
			.merge(sorting)
			.flatMap(merged -> s.fetch(merged.getDocTypeField()))
		);
	}

	public Uni<DocTypeField> getDocTypeField(long sortingId) {
		return sessionFactory.withTransaction(s -> findById(sortingId)
			.flatMap(t -> s.fetch(t.getDocTypeField())));
	}

	public Uni<Tuple2<Sorting, DocTypeField>> bindDocTypeFieldToSorting(
		long sortingId, long docTypeFieldId) {
		return sessionFactory.withTransaction((s) -> findById(s, sortingId)
			.onItem()
			.ifNotNull()
			.transformToUni(sorting -> docTypeFieldService.findById(s, docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> {
					sorting.setDocTypeField(docTypeField);
					return persist(s, sorting)
						.map(newSorting -> Tuple2.of(newSorting, docTypeField));
				})
			)
		);
	}

	public Uni<Tuple2<Sorting, DocTypeField>> unbindDocTypeFieldFromSorting(
		long sortingId, long docTypeFieldId) {
		return sessionFactory.withTransaction((s) -> findById(s, sortingId)
			.onItem()
			.ifNotNull()
			.transformToUni(sorting -> docTypeFieldService.findById(s, docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> {
					sorting.setDocTypeField(null);
					return persist(s, sorting)
						.map(newSorting -> Tuple2.of(newSorting, docTypeField));
				})));
	}


	public Uni<Connection<DocTypeField>> getDocTypeFieldsNotInSorting(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList) {
		return findJoinConnection(
			id, Sorting_.DOC_TYPE_FIELD, DocTypeField.class,
			docTypeFieldService.getSearchFields(), after, before, first, last,
			searchText, sortByList, true);
	}

	public Uni<Void> addTranslation(Long id, TranslationDTO dto) {
		return translationService.addTranslation(
			Sorting.class, id, dto.getLanguage(), dto.getKey(), dto.getValue());
	}

	public Uni<Void> deleteTranslation(Long id, TranslationKeyDTO dto) {
		return translationService.deleteTranslation(
			Sorting.class, id, dto.getLanguage(), dto.getKey());
	}

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	TranslationService translationService;

}

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

import io.openk9.datasource.mapper.SuggestionCategoryMapper;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.dto.SuggestionCategoryDTO;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.function.Function;

@ApplicationScoped
public class SuggestionCategoryService extends
	BaseK9EntityService<SuggestionCategory, SuggestionCategoryDTO> {
	 SuggestionCategoryService(SuggestionCategoryMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<Collection<SuggestionCategory>> findByTenantId(long tenantId) {
		return SuggestionCategory.<SuggestionCategory>list("tenant_id", tenantId)
			.map(Function.identity());
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long suggestionCategoryId, Pageable pageable) {
		 return getDocTypeFields(
			suggestionCategoryId, pageable, Filter.DEFAULT);
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long suggestionCategoryId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[] { suggestionCategoryId },
			"docTypeFields", DocTypeField.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(), filter
		);
	}

	public Uni<Tuple2<SuggestionCategory, DocTypeField>> addDocTypeField(
		long suggestionCategoryId, long docTypeFieldId) {
		return findById(suggestionCategoryId)
			.onItem()
			.ifNotNull()
			.transformToUni(suggestionCategory -> docTypeFieldService.findById(docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> Mutiny.fetch(suggestionCategory.getDocTypeFields()).flatMap(docTypeFields ->{
					if (docTypeFields.add(docTypeField)) {
						suggestionCategory.setDocTypeFields(docTypeFields);
						return persist(suggestionCategory).map(sc -> Tuple2.of(sc, docTypeField));
					}
					return Uni.createFrom().nullItem();

				})));
	}

	public Uni<Tuple2<SuggestionCategory, DocTypeField>> removeDocTypeField(
		long suggestionCategoryId, long docTypeFieldId) {
		return findById(suggestionCategoryId)
			.onItem()
			.ifNotNull()
			.transformToUni(suggestionCategory -> docTypeFieldService.findById(docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> Mutiny.fetch(suggestionCategory.getDocTypeFields()).flatMap(docTypeFields ->{
					if (docTypeFields.remove(docTypeField)) {
						suggestionCategory.setDocTypeFields(docTypeFields);
						return persist(suggestionCategory).map(sc -> Tuple2.of(sc, docTypeField));
					}
					return Uni.createFrom().nullItem();

				})));
	}

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Override
	public Class<SuggestionCategory> getEntityClass() {
		return SuggestionCategory.class;
	}
}
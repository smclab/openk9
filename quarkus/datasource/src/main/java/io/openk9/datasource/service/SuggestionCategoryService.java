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
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

		Map<String, Object> params = new HashMap<>();

		params.put("suggestionCategoryId", suggestionCategoryId);

		String query =
			"select dtf " +
			"from SuggestionCategory sc " +
			"join sc.docTypeFields dtf " +
			"where sc.id = :suggestionCategoryId ";

		query = createPageableQuery(pageable, params, query, "dtf");

		Sort sort = createSort("dtf", pageable.getSortBy().name());

		PanacheQuery<DocTypeField> docTypeFieldPanacheQuery =
			SuggestionCategory
				.find(query, sort, params)
				.page(0, pageable.getLimit());

		 Uni<Long> countQuery = SuggestionCategory
			 .count("from SuggestionCategory sc join sc.docTypeFields where sc.id = ?1", suggestionCategoryId);

		return createPage(
			pageable.getLimit(), docTypeFieldPanacheQuery, countQuery);
	}

	public Uni<Void> addDocTypeField(
		long suggestionCategoryId, long docTypeFieldId) {
		return findById(suggestionCategoryId)
			.flatMap(suggestionCategory -> docTypeFieldService.findById(docTypeFieldId)
				.flatMap(docTypeField -> {
					suggestionCategory.addDocTypeField(docTypeField);
					return persist(suggestionCategory);
				}))
				.replaceWithVoid();
	}

	public Uni<Void> removeDocTypeField(
		long suggestionCategoryId, long docTypeFieldId) {
		return findById(suggestionCategoryId)
			.flatMap(suggestionCategory -> docTypeFieldService.findById(docTypeFieldId)
				.flatMap(docTypeField -> {
					suggestionCategory.removeDocTypeField(docTypeField);
					return persist(suggestionCategory);
				}))
				.replaceWithVoid();
	}

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Override
	public Class<SuggestionCategory> getEntityClass() {
		return SuggestionCategory.class;
	}
}